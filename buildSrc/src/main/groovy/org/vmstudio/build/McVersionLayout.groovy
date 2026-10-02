package org.vmstudio.build

import groovy.io.FileType
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider

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

    // parked copies are in the form of their range's first version: gates and renames are brought to the node's
    TaskProvider<Sync> parkedSources(Project project, String version) {
        def parked = parkedFiles().findAll { it.range?.contains(version) }
        project.tasks.register("mcversionParkedSources", Sync) { Sync task ->
            task.description = "Copies the parked range files covering ${version}, rendered and renamed for it"
            parked.groupBy { it.root }.each { root, files ->
                task.from(root) { include(files*.rel) }
            }
            task.into(project.layout.buildDirectory.dir("mcversion/java"))
            task.inputs.property("renames", McVersionRenames.signature(version))
            task.filteringCharset = "UTF-8"
            task.filter(McVersionParkedFilter, version: version)
        }
    }

    List<String> excludes(String version) {
        headerFiles().findAll { rel, range -> !range.contains(version) }.keySet().toList()
    }


    // by header, not by location: the parked copy that covers the version shares the rel path
    Spec<FileTreeElement> excludeSpec(String version) {
        Set<String> excluded = excludes(version) as Set
        return { FileTreeElement e ->
            !e.directory && excluded.contains(e.relativePath.pathString) && !McVersionRange.fromHeader(e.file)?.contains(version)
        } as Spec<FileTreeElement>
    }

    List<String> check(String active) {
        def problems = []
        def copies = [:]    // rel -> [[where, range]]
        headerFiles().each { rel, range ->
            def where = "src/main/java/${rel}"
            if (!rel.startsWith("org/vmstudio/visor/mixin/")) {
                problems << "${where}: range files are for the core mixins only"
            }
            if (!range.contains(active)) {
                problems << "${where}: declares ${range} but the active version is ${active} - run the switch"
            }
            problems.addAll(gateProblems(where, new File(src, rel), range, active, "refresh the active project"))
            copies.computeIfAbsent(rel) { [] } << [where, range]
        }
        parkedFiles().each { p ->
            def where = "mcversion/${p.folder}/java/${p.rel}"
            if (!p.rel.startsWith("org/vmstudio/visor/mixin/")) {
                problems << "${where}: range files are for the core mixins only"
            }
            if (p.range == null) {
                problems << "${where}: missing the '${McVersionRange.HEADER} <range>' header"
                return
            }
            p.range.bounds.each { bound ->
                if (!(bound in nodes)) {
                    problems << "${where}: ${bound} is not a ${branch.name} target"
                }
            }
            if (p.folder != p.range.from) {
                problems << "${where}: declares ${p.range}, belongs in mcversion/${p.range.from}"
            }
            problems.addAll(gateProblems(where, p.file, p.range, p.range.from, "run mcversionSwitch"))
            if (p.range.contains(active)) {
                problems << "${where}: parked although ${p.range} covers the active version ${active} - run the switch"
            }
            copies.computeIfAbsent(p.rel) { [] } << [where, p.range]
        }
        copies.each { rel, list ->
            list.eachWithIndex { a, i ->
                list.drop(i + 1).each { b ->
                    def shared = nodesIn(a[1]).intersect(nodesIn(b[1]))
                    if (shared) {
                        problems << "${a[0]} (${a[1]}) and ${b[0]} (${b[1]}) both cover ${shared.join(', ')}"
                    }
                }
            }
        }
        problems
    }


    // before Stonecutter processes src for the version: it renders the arrivals itself, files are only moved
    List<String> moveTo(String version) {
        def moves = moves(version)
        (moves.leaving + moves.entering).collect { File from, File to -> move(from, to) }
    }

    // after Stonecutter (or standalone, to resync): moves what is still misplaced, renders those arrivals, and puts
    // every parked copy in the form of its range's first version, so round trips stay clean
    List<String> switchTo(String version) {
        def moves = moves(version)
        def log = (moves.leaving + moves.entering).collect { File from, File to -> move(from, to) }
        moves.entering.each { File from, File to ->
            if (normalize(to, version)) {
                log << "${rel(branch, to)}: rendered for ${version}".toString()
            }
        }
        parkedFiles().each { p ->
            if (normalize(p.file, p.range.from)) {
                log << "${rel(branch, p.file)}: rendered for ${p.range.from}".toString()
            }
        }
        log
    }

    private Map<String, List<List<File>>> moves(String version) {
        def parked = parkedFiles()
        parked.findAll { it.range == null }.each {
            throw new GradleException("mcversion/${it.folder}/java/${it.rel}: missing the '${McVersionRange.HEADER} <range>' header")
        }
        def leaving = headerFiles().findAll { rel, range -> !range.contains(version) }
                .collect { rel, range -> [new File(src, rel), parkedFile(range, rel)] }
        def entering = parked.findAll { it.range.contains(version) }
                .collect { [it.file, new File(src, it.rel)] }
        // leaving moves run first: a src file they vacate can be refilled, a parked target must be free
        def vacated = leaving.collect { it[0] } as Set
        def targets = [] as Set
        (leaving + entering).each { File from, File to ->
            if (to.exists() && !(to in vacated)) {
                throw new GradleException("${rel(branch, to)} exists already")
            }
            if (!targets.add(to)) {
                throw new GradleException("two range files would land on ${rel(branch, to)}")
            }
        }
        [leaving: leaving, entering: entering]
    }

    private String move(File from, File to) {
        to.parentFile.mkdirs()
        Files.move(from.toPath(), to.toPath())
        pruneEmpty(from.parentFile)
        "${rel(branch, from)} -> ${rel(branch, to)}".toString()
    }

    List<String> nodesIn(McVersionRange range) {
        nodes.findAll { range.contains(it) }
    }

    // the only copy of a parked range file sits in the folder of its range's first version
    private File parkedFile(McVersionRange range, String rel) {
        new File(parkingLot, "${range.from}/java/${rel}")
    }

    private List<Map> parkedFiles() {
        def out = []
        (parkingLot.listFiles() ?: new File[0]).findAll { it.directory }.sort { it.name }.each { dir ->
            def root = new File(dir, "java")
            if (root.directory) {
                root.eachFileRecurse(FileType.FILES) { f ->
                    if (f.name.endsWith(".java")) {
                        out << [folder: dir.name, root: root, rel: rel(root, f), file: f, range: McVersionRange.fromHeader(f)]
                    }
                }
            }
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

    // what Stonecutter would make of the file on that version: gates rendered, McVersionRenames applied
    static String forVersion(String text, String version) {
        McVersionRenames.apply(McVersionGates.render(text, version), version)
    }

    private List<String> gateProblems(String where, File f, McVersionRange range, String version, String fix) {
        def text = f.getText("UTF-8")
        try {
            def problems = []
            if (forVersion(text, version) != text) {
                problems << "${where}: gates or names not in the form of ${version} - ${fix}".toString()
            }
            McVersionGates.constantChains(text, nodesIn(range)).each { marker ->
                problems << "${where}: '${marker}' decides the same on every version of ${range}, resolve it".toString()
            }
            problems
        } catch (IllegalArgumentException e) {
            ["${where}: ${e.message}".toString()]
        }
    }

    private static boolean normalize(File f, String version) {
        def text = f.getText("UTF-8")
        def normalized = forVersion(text, version)
        if (normalized == text) {
            return false
        }
        f.setText(normalized, "UTF-8")
        true
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
