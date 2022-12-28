package io.github.rmaiun.microsaga.mta;

import net.jodah.failsafe.RetryPolicy;

import java.util.function.Consumer;
/**
 * Creado por Christian Candela
 *
 */
public class CompensationBuilder {
    private String name;
    private Runnable compensation = () -> { };
    private Consumer<String> compensation0 = mtaId -> compensation.run();
    private RetryPolicy<Object> retryPolicy = new RetryPolicy<>().withMaxRetries(0);
    private boolean technical = false;

    public static CompensationBuilder create(){
        return new CompensationBuilder();
    }
    public static CompensationBuilder technical(){
        return CompensationBuilder.create().name("").technical(true);
    }
    public static CompensationBuilder emptyCompensation(String name){
        return CompensationBuilder.create().name(name);
    }
    public CompensationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CompensationBuilder compensation(Runnable compensation) {
        this.compensation = compensation;
        return this;
    }

    public CompensationBuilder compensation(Consumer<String> compensation0) {
        this.compensation0 = compensation0;
        return this;
    }

    public CompensationBuilder retryPolicy(int maxRetries) {
        this.retryPolicy = new RetryPolicy<>().withMaxRetries(maxRetries);;
        return this;
    }

    public CompensationBuilder retryPolicy(RetryPolicy<Object> retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    public CompensationBuilder technical(boolean technical) {
        this.technical = technical;
        return this;
    }

    public Compensation build() {
        return new Compensation(name, compensation0, retryPolicy, technical);
    }
}