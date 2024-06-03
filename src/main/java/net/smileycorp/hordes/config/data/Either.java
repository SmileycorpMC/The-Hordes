package net.smileycorp.hordes.config.data;

import java.util.function.Function;

public abstract class Either<L, R> {
    
    public static <L, R>  Either<L, R> left(L value) {
        return new Left(value);
    }
    
    public static <L, R>  Either<L, R> right(R value) {
        return new Right(value);
    }
    
    public abstract <T> T map(Function<L, T> l, Function<R, T> r);
    
    private static class Left<L, R> extends Either<L, R> {
    
        private final L value;
    
        private Left(L value) {
            this.value = value;
        }
    
        @Override
        public <T> T map(Function<L, T> l, Function<R, T> r) {
            return l.apply(value);
        }
        
    }
    
    private static class Right<L, R> extends Either<L, R> {
        
        private final R value;
        
        private Right(R value) {
            this.value = value;
        }
        
        @Override
        public <T> T map(Function<L, T> l, Function<R, T> r) {
            return r.apply(value);
        }
        
    }
    
}
