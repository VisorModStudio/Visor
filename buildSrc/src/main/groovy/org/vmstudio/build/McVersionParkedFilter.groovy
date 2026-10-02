package org.vmstudio.build


// whole-file filter of the parked range files of a node build: gates rendered and names renamed for the node
class McVersionParkedFilter extends FilterReader {
    String version
    private final Reader source
    private Reader rendered

    McVersionParkedFilter(Reader source) {
        super(source)
        this.source = source
    }

    private Reader rendered() {
        if (rendered == null) {
            def text = new StringWriter()
            source.transferTo(text)
            rendered = new StringReader(McVersionLayout.forVersion(text.toString(), version))
        }
        rendered
    }

    @Override
    int read() {
        rendered().read()
    }

    @Override
    int read(char[] buffer, int offset, int length) {
        rendered().read(buffer, offset, length)
    }

    @Override
    long skip(long n) {
        rendered().skip(n)
    }

    @Override
    boolean ready() {
        rendered().ready()
    }

    @Override
    boolean markSupported() {
        false
    }
}
