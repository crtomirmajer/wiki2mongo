package com.crtomirmajer.wiki2mongo.store;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Morphia;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Majer on 30.10.2016.
 */
public class MongoDatastore implements Datastore {
    
    private static final String DEFAULT_DATABASE = "wikipedia";
    
    private AdvancedDatastore datastore;
    private WriteConcern      writeConcern;
    
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    private MongoDatastore(Builder builder) {
        datastore = builder.datastore;
        writeConcern = builder.writeConcern;
    }
    
    @Override
    public CompletableFuture<List<Page>> save(List<Page> pages) {
        final CompletableFuture<List<Page>> promise = new CompletableFuture<>();
        
        executor.submit(() -> {
            datastore.save(pages, writeConcern);
            promise.complete(pages);
        });
        
        return promise;
    }
    
    public static final class Builder {
        
        private MongoClient mongoClient;
        private WriteConcern writeConcern = WriteConcern.JOURNALED;
        private String       database     = DEFAULT_DATABASE;
        
        private AdvancedDatastore datastore;
        
        public Builder() {
        }
        
        public MongoDatastore build() {
            
            if(mongoClient == null)
                throw new IllegalArgumentException("Mongo client must not be null.");
            
            final Morphia morphia = new Morphia();
            morphia.mapPackage("com.crtomirmajer.wiki2mongo");
            datastore = (AdvancedDatastore) morphia.createDatastore(mongoClient, database);
            
            return new MongoDatastore(this);
        }
        
        public Builder client(MongoClient mongoClient) {
            if(mongoClient == null)
                throw new IllegalArgumentException("Mongo client must not be null.");
            
            this.mongoClient = mongoClient;
            return this;
        }
        
        public Builder database(String database) {
            
            if(database == null || database.isEmpty())
                throw new IllegalArgumentException("Database name must not be empty.");
            
            this.database = database;
            return this;
        }
        
        public Builder writeConcern(WriteConcern writeConcern) {
            
            if(writeConcern == null)
                throw new IllegalArgumentException("WriteConcern must not be null.");
            
            this.writeConcern = writeConcern;
            return this;
        }
    }
}
