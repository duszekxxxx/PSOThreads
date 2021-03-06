package mainPackage;

import java.util.concurrent.*;
import java.util.*;

public class Swarm {
    private int numOfParticles, epochs;
    private double inertia, cognitiveComponent, socialComponent;
    private double[] bestGlobalPosition;
    private double bestGlobalEval;
    private double propDeath, exitError;
    private Function function;

    public static final double DEFAULT_INERTIA = 0.729844;
    public static final double DEFAULT_COGNITIVE = 1.496180;
    public static final double DEFAULT_SOCIAL = 1.496180;

    public Swarm(Function function, int numParticles, int epochs, double exitError, double randomDeath) {
        this(function, numParticles, epochs, exitError, randomDeath, DEFAULT_INERTIA, DEFAULT_COGNITIVE,
                DEFAULT_SOCIAL);
    }

    public Swarm(Function function, int numParticles, int epochs, double exitError, double propDeath, double inertia,
            double cognitive, double social) {
        this.numOfParticles = numParticles;
        this.epochs = epochs;
        this.exitError = exitError;
        this.propDeath = propDeath;
        this.inertia = inertia;
        this.cognitiveComponent = cognitive;
        this.socialComponent = social;
        this.function = function;
        bestGlobalEval = Double.MAX_VALUE;
    }

    public Particle[] initialize() {
        Particle[] particles = new Particle[numOfParticles];

        for (int i = 0; i < numOfParticles; i++) {
            Particle p = new Particle(function, propDeath, inertia, cognitiveComponent, socialComponent);
            p.updatePersonalBest();
            updateGlobalAndPersonalBest(p, -1);
            particles[i] = p;
        }

        return particles;
    }

    private Callable<Boolean> createRunnable(final Particle p) {
        Callable<Boolean> runnable = new Callable<Boolean>() {
            public Boolean call() {
                p.updateVelocity(bestGlobalPosition);
                p.updatePosition();
                p.updatePersonalBest();
                return true;
            }
        };

        return runnable;
    }

    public void runWithExecutor() throws InterruptedException, ExecutionException {
        System.out.println("--------------------------EXECUTING-------------------------");

        Particle[] particles = initialize();
        boolean hasFoundTheBest = false;
        System.out.println("Global Best Evaluation (Epoch " + 0 + "):\t" + bestGlobalEval);
        List<ParticleThread> runnables = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (Particle p : particles) {
            ParticleThread task = new ParticleThread(p,bestGlobalPosition);
            runnables.add(task);
            threads.add(new Thread(task));
        }
        for (int i = 0; i < epochs; i++) {
            for (Particle p : particles) {
                ParticleThread task = new ParticleThread(p,bestGlobalPosition);
                runnables.add(task);
                Thread t = new Thread(task);
                t.start();
                threads.add(t);

            }
            for (Thread t : threads) {
                t.join();
            }
            threads.clear();
            for (ParticleThread r : runnables) {
                hasFoundTheBest = updateGlobalAndPersonalBest(r.getParticle(), i);
                if (hasFoundTheBest)
                    break;
            }
            for (ParticleThread r : runnables) {
                r.setBestGlobalPosition(this.bestGlobalPosition);
            }

            /*
             * Jak komu?? si?? chce, to mo??e jeszcze podpi???? to do w??tk??w boolean
             * particleDeath = p.randomDeath(); if (particleDeath) { p.updatePersonalBest();
             *
             * hasFoundTheBest = updateGlobalAndPersonalBest(p, i); if (hasFoundTheBest)
             * break; }
             */

            if (hasFoundTheBest)
                break;
        }

    }

    public void runWithoutExecutor() {
        System.out.println("--------------------------EXECUTING-------------------------");

        Particle[] particles = initialize();

        boolean hasFoundTheBest = false;

        System.out.println("Global Best Evaluation (Epoch " + 0 + "):\t" + bestGlobalEval);

        for (int i = 0; i < epochs; i++) {
            for (Particle p : particles) {
                p.updateVelocity(bestGlobalPosition);
                p.updatePosition();
                p.updatePersonalBest();

                hasFoundTheBest = updateGlobalAndPersonalBest(p, i);
                if (hasFoundTheBest)
                    break;

                boolean particleDeath = p.randomDeath();
                if (particleDeath) {
                    p.updatePersonalBest();

                    hasFoundTheBest = updateGlobalAndPersonalBest(p, i);
                    if (hasFoundTheBest)
                        break;
                }
            }

            if (hasFoundTheBest)
                break;
        }
    }

    public boolean updateGlobalAndPersonalBest(Particle p, int i) {
        double eval = p.getBestEval();
        boolean isNewGlobal = eval < bestGlobalEval;

        if (isNewGlobal) {
            bestGlobalPosition = p.getClonePosition();
            bestGlobalEval = eval;

            if (i != -1) {
                System.out.println("Global Best Evaluation (Epoch " + (i + 1) + "):\t" + bestGlobalEval);
            }
        }

        return eval <= exitError;
    }

    public double[] GetBestPosition() {
        return bestGlobalPosition;
    }

    public double GetBestEval() {
        return bestGlobalEval;
    }
}
