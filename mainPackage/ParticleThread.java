package mainPackage;

public class ParticleThread implements Runnable{

    private Particle particle;
    private double[] bestGlobalPosition;

    public ParticleThread(Particle particle,double[] bestGlobalPosition) {
        this.bestGlobalPosition = bestGlobalPosition;
        this.particle = particle;
    }

    @Override
    public void run() {
        particle.updateVelocity(bestGlobalPosition);
        particle.updatePosition();
        particle.updatePersonalBest();
    }

    public void setBestGlobalPosition(double[] bestGlobalPosition) {
        this.bestGlobalPosition = bestGlobalPosition;
    }

    public Particle getParticle() {
        return particle;
    }
}
