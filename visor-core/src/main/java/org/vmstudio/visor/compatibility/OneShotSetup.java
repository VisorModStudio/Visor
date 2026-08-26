package org.vmstudio.visor.compatibility;

/**
 * Runs a reflective setup step at most once and remembers whether it worked
 */
public class OneShotSetup {
    // the lookup itself
    public interface Step {
        boolean run() throws ReflectiveOperationException;
    }

    private final Step step;
    private volatile Boolean usable;

    public OneShotSetup(Step step) {
        this.step = step;
    }

    public boolean ok() {
        Boolean known = usable;
        if (known != null) {
            return known;
        }
        synchronized (this) {
            if (usable == null) {
                usable = attempt();
            }
            return usable;
        }
    }

    public void disable() {
        usable = Boolean.FALSE;
    }

    private boolean attempt() {
        try {
            return step.run();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return false;
        }
    }
}
