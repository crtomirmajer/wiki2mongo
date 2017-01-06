package com.crtomirmajer.wiki2mongo.clean;

import com.crtomirmajer.wiki2mongo.store.Page;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Majer on 30.10.2016.
 */
public class CleanerService {
    
    private Cleaner cleaner;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    public CleanerService(Cleaner cleaner) {
        this.cleaner = cleaner;
    }
    
    public CompletableFuture<Page> clean(final Page page) {
        
        final CompletableFuture<Page> promise = new CompletableFuture<>();
        
        if(page.getText() == null)
            promise.complete(page);
        else
            executor.submit(() -> {
                page.setText(cleaner.clean(page.getText()));
                promise.complete(page);
            });
        
        return promise;
    }
}
