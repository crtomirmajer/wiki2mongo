package com.crtomirmajer.wiki2mongo.io;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.crtomirmajer.wiki2mongo.clean.Cleaner;
import com.crtomirmajer.wiki2mongo.clean.CleanerService;
import com.crtomirmajer.wiki2mongo.store.Datastore;
import com.crtomirmajer.wiki2mongo.store.Page;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Majer on 30.10.2016.
 */
public class Wiki2MongoImporter {
    
    private static final int BATCH_SIZE = 100;
    
    private WikiDumpReader reader;
    private Datastore      datastore;
    private CleanerService cleaner;
    
    private int cores = Runtime.getRuntime().availableProcessors();
    
    private Wiki2MongoImporter(Builder builder) {
        reader = builder.reader;
        datastore = builder.datastore;
        cleaner = new CleanerService(builder.cleaner);
    }
    
    public CompletableFuture<Done> importAll() {
        
        AtomicLong batch = new AtomicLong(0);
        long start = System.currentTimeMillis();
        
        final Materializer materializer = ActorMaterializer.create(ActorSystem.create("actor-system", ConfigFactory.load()));
        Source<Page, NotUsed> channelSource = Source.fromIterator(() -> reader);
        
        Flow<Page, List<Page>, NotUsed> processing = Flow.of(Page.class)
                                                         .filter(x -> x != null && x.getNamespace() == 0)
                                                         .buffer(cores * 100, OverflowStrategy.backpressure())
                                                         .mapAsyncUnordered(cores, x -> cleaner.clean(x))
                                                         .groupedWithin(BATCH_SIZE,
                                                                        FiniteDuration.apply(1, TimeUnit.SECONDS))
                                                         .mapAsyncUnordered(cores, x -> datastore.save(x));
        
        final CompletionStage<Done> promise = channelSource.via(processing).runForeach(x -> {
            
            System.out.printf("Inserted: %d, Time: %d sec\n",
                              batch.incrementAndGet() * BATCH_SIZE,
                              (System.currentTimeMillis() - start) / 1000);
        }, materializer);
        
        return promise.toCompletableFuture();
        
    }
    
    public static final class Builder {
        
        private WikiDumpReader reader;
        private Datastore      datastore;
        private Cleaner        cleaner;
        
        public Builder() {}
        
        public Wiki2MongoImporter build() {
            return new Wiki2MongoImporter(this);
        }
        
        public Builder reader(WikiDumpReader reader) {
            this.reader = reader;
            return this;
        }
        
        public Builder datastore(Datastore datastore) {
            this.datastore = datastore;
            return this;
        }
        
        public Builder cleaner(Cleaner cleaner) {
            this.cleaner = cleaner;
            return this;
        }
    }
}
