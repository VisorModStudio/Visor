package org.vmstudio.build

import groovy.io.FileType
import org.gradle.api.GradleException
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec

import java.nio.file.Files
import java.util.regex.Pattern


class McVersionLayout {
    private static final Pattern ACTIVE = ~/stonecutter\.active\s+"([^"]+)"/

    final File branch
    final List<String> nodes

    McVersionLayout(File branch, Collection<String> nodes) {
        this.branch = branch
        this.nodes = nodes.toList()
    }

    File getSrc() {
        new File(branch, "src/main/java")
    }

    File getParkingLot() {
        new File(branch, "mcversion")
    }

    static String activeVersion(File rootDir) {
        def m = ACTIVE.matcher(new File(rootDir, "stonecutter.gradle").getText("UTF-8"))
        if (!m.find()) {
            throw new GradleException("stonecutter.gradle: no stonecutter.active line")
        }
        m.group(1)
    }

    List<File> parkedSourceDirs(String version, String kind) {
        def dir = new File(parkingLot, "${version}/${kind}")
        dir.directory ? [dir] : []
    }

    List<String> excludes(String version) {
        headerFiles().findAll { rel, range -> !range.contains(version) }.keySet().toList()
    }


    Spec<FileTreeElement> excludeSpec(String version) {
        Set<String> excluded = excludes(version) as Set
        String lot = parkingLot.absolutePath + File.separator
        return { FileTreeElement e ->
            !e.directory && excluded.contains(e.relativePath.pathString) && !e.file.absolutePath.startsWith(lot)
        } as Spec<FileTreeElement>
    }

    List<String> check(String active) {
        def problems = []
        def headers = headerFiles()
        headers.each { rel, range ->
            def where = "src/main/java/${rel}"
            if (!range.contains(active)) {
                problems << "${where}: declares ${range} but the active version is ${active} - run the switch"
            }
            if (hasMarkers(new File(src, rel))) {
                problems << "${where}: range files are never preprocessed, no Stonecutter markers"
            }
            nodesIn(range).each { node ->
                if (new File(parkingLot, "${node}/java/${rel}").file) {
                    problems << "mcversion/${node}/java/${rel}: stale copy, src covers ${node} - run the switch"
                }
            }
        }
        def parked = parkedFiles()
        parked.each { node, files ->
            files.each { rel, f ->
                def where = "mcversion/${node}/java/${rel}"
                def range = McVersionRange.fromHeader(f)
                if (range == null) {
                    problems << "${where}: missing the '${McVersionRange.HEADER} <range>' header"
                    return
                }
                range.bounds.each { bound ->
                    if (!(bound in nodes)) {
                        problems << "${where}: ${bound} is not a ${branch.name} target"
                    }
                }
                if (!range.contains(node)) {
                    problems << "${where}: declares ${range}, which does not cover ${node}"
                }
                if (range.contains(active)) {
                    problems << "${where}: parked although ${range} covers the active version ${active} - run the switch"
                }
                if (hasMarkers(f)) {
                    problems << "${where}: range files are never preprocessed, no Stonecutter markers"
                }
                def headerCopy = headers[rel]
                if (headerCopy != null && headerCopy.contains(node)) {
                    problems << "${where}: src/main/java/${rel} (${headerCopy}) covers ${node} as well"
                }
                nodesIn(range).each { other ->
                    def twin = parked[other]?.get(rel)
                    if (twin == null) {
                        problems << "${where}: ${range} covers ${other} but mcversion/${other} has no copy - run the switch"
                    } else if (other > node && twin.bytes != f.bytes) {
                        problems << "${where} and mcversion/${other}/java/${rel} differ although both declare ${range}"
                    }
                }
            }
        }
        problems
    }


    List<String> switchTo(String version) {
        if (!(version in nodes)) {
            throw new GradleException("${version} is not a ${branch.name} target")
        }
        def leaving = [:]    // src file -> [node folders to copy into]
        def entering = []    // [parked file, src target, range]
        headerFiles().each { rel, range ->
            if (!range.contains(version)) {
                leaving[new File(src, rel)] = nodesIn(range).collect { new File(parkingLot, "${it}/java/${rel}") }
            }
        }
        def own = parkedFiles()[version] ?: [:]
        own.each { rel, f ->
            def range = McVersionRange.fromHeader(f)
            if (range == null || !range.contains(version)) {
                throw new GradleException("mcversion/${version}/java/${rel}: header ${range} does not cover ${version}")
            }
            entering << [f, new File(src, rel), range]
        }
        def vacated = leaving.keySet()
        def targets = [] as Set
        (leaving.values().flatten() + entering.collect { it[1] }).each { File to ->
            if (to.exists() && !(to in vacated)) {
                throw new GradleException("${rel(branch, to)} exists already")
            }
            if (!targets.add(to)) {
                throw new GradleException("two range files would land on ${rel(branch, to)}")
            }
        }
        def log = []
        leaving.each { File from, List<File> copies ->
            copies.each { File to ->
                to.parentFile.mkdirs()
                Files.copy(from.toPath(), to.toPath())
                log << "${rel(branch, from)} -> ${rel(branch, to)}".toString()
            }
            Files.delete(from.toPath())
            pruneEmpty(from.parentFile)
        }
        entering.each { File from, File to, McVersionRange range ->
            to.parentFile.mkdirs()
            Files.move(from.toPath(), to.toPath())
            log << "${rel(branch, from)} -> ${rel(branch, to)}".toString()
            pruneEmpty(from.parentFile)
            // the other folders of the range hold the same bytes: src is the only copy while it is active
            nodesIn(range).findAll { it != version }.each { other ->
                def twin = new File(parkingLot, "${other}/java/${rel(src, to)}")
                if (twin.file) {
                    Files.delete(twin.toPath())
                    log << "removed ${rel(branch, twin)}".toString()
                    pruneEmpty(twin.parentFile)
                }
            }
        }
        log
    }

    List<String> nodesIn(McVersionRange range) {
        nodes.findAll { range.contains(it) }
    }

    private Map<String, Map<String, File>> parkedFiles() {
        def out = new TreeMap<String, Map<String, File>>()
        (parkingLot.listFiles() ?: new File[0]).findAll { it.directory }.each { dir ->
            if (!(dir.name in nodes)) {
                throw new GradleException("mcversion/${dir.name}: not a ${branch.name} target")
            }
            def files = new TreeMap<String, File>()
            def java = new File(dir, "java")
            if (java.directory) {
                java.eachFileRecurse(FileType.FILES) { f ->
                    if (f.name.endsWith(".java")) {
                        files[rel(java, f)] = f
                    }
                }
            }
            out[dir.name] = files
        }
        out
    }

    private Map<String, McVersionRange> headerFiles() {
        def found = new TreeMap<String, McVersionRange>()
        if (!src.directory) {
            return found
        }
        src.eachFileRecurse(FileType.FILES) { f ->
            if (!f.name.endsWith(".java")) {
                return
            }
            def header = McVersionRange.fromHeader(f)
            if (header != null) {
                header.bounds.each { bound ->
                    if (!(bound in nodes)) {
                        throw new GradleException("${rel(branch, f)}: ${bound} is not a ${branch.name} target")
                    }
                }
                found[rel(src, f)] = header
            }
        }
        found
    }

    private static boolean hasMarkers(File f) {
        def text = f.getText("UTF-8")
        text.contains("//?") || text.contains("/*?")
    }

    private void pruneEmpty(File dir) {
        def stop = [src, parkingLot]*.canonicalFile
        def d = dir.canonicalFile
        while (d != null && !(d in stop) && d.directory && (d.list()?.length ?: 0) == 0) {
            d.delete()
            d = d.parentFile
        }
    }

    private static String rel(File root, File f) {
        root.toPath().relativize(f.toPath()).toString().replace('\\', '/')
    }
}
