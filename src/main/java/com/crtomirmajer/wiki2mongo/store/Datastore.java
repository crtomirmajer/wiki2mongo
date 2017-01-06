package com.crtomirmajer.wiki2mongo.store;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Majer on 30.10.2016.
 */
public interface Datastore {
    
    CompletableFuture<List<Page>> save(List<Page> pages);
    
}
