package org.vmstudio.build

import groovy.io.FileType


// the gated sources Stonecutter processes (src/main/java, access wideners) in McVersionGates.nativeForm of the active
// version: a switch renames commented branches to the active version, this puts them back after it
class McVersionForm {
    final File root
    final Map<String, List<String>> branches

    McVersionForm(File root, Map<String, List<String>> branches) {
        this.root = root
        this.branches = branches
    }

    List<String> normalize(String active) {
        def log = []
        gatedFiles().each { File f, List<String> nodes ->
            def text = f.getText("UTF-8")
            def form = canonical(f, text, active, nodes)
            if (form != text) {
                f.setText(form, "UTF-8")
                log << "${rel(f)}: commented branches back in their own names".toString()
            }
        }
        log
    }

    List<String> check(String active) {
        def problems = []
        gatedFiles().each { File f, List<String> nodes ->
            if (McVersionRange.fromHeader(f) != null) {
                return  // McVersionLayout.check reports the range files
            }
            def text = f.getText("UTF-8")
            try {
                if (canonical(f, text, active, nodes) != text) {
                    problems << "${rel(f)}: gates or names not in the form of ${active} - refresh the active project".toString()
                }
            } catch (IllegalArgumentException e) {
                problems << "${rel(f)}: ${e.message}".toString()
            }
        }
        problems
    }

    private static String canonical(File f, String text, String active, List<String> nodes) {
        if (f.name.endsWith(".java")) {
            return McVersionGates.nativeForm(McVersionLayout.forVersion(text, active), active, nodes)
        }
        // Stonecutter renders the '#' gates, the names are put in place here
        McVersionGates.nativeForm(text, active, nodes, McVersionGates.HASH_MARKER)
    }

    // file -> the nodes it is built for (a range file in src: those of its range)
    private Map<File, List<String>> gatedFiles() {
        def out = new LinkedHashMap<File, List<String>>()
        branches.each { String name, List<String> nodes ->
            def java = new File(root, "${name}/src/main/java")
            if (java.directory) {
                java.eachFileRecurse(FileType.FILES) { f ->
                    if (f.name.endsWith(".java") && McVersionGates.hasMarkers(f.getText("UTF-8"))) {
                        def range = McVersionRange.fromHeader(f)
                        out[f] = range == null ? nodes : nodes.findAll { range.contains(it) }
                    }
                }
            }
            def resources = new File(root, "${name}/src/main/resources")
            if (resources.directory) {
                resources.eachFileRecurse(FileType.FILES) { f ->
                    if (f.name.endsWith(".accesswidener") && McVersionGates.HASH_MARKER.matcher(f.getText("UTF-8")).find()) {
                        out[f] = nodes
                    }
                }
            }
        }
        out
    }

    private String rel(File f) {
        root.toPath().relativize(f.toPath()).toString().replace('\\', '/')
    }
}
