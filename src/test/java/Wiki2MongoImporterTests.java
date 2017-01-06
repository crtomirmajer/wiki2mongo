import com.crtomirmajer.wiki2mongo.clean.MarkupCleaner;
import com.crtomirmajer.wiki2mongo.io.Wiki2MongoImporter;
import com.crtomirmajer.wiki2mongo.io.WikiDumpReader;
import com.crtomirmajer.wiki2mongo.store.MongoDatastore;
import com.mongodb.MongoClient;

/**
 * Created by Majer
 */
public class Wiki2MongoImporterTests {
    
    //Set this path to wiki dump XML file
    private String wikiDumpPath = "";
    
    public void importAll() throws Exception {
        
        WikiDumpReader reader = new WikiDumpReader(wikiDumpPath);
        Wiki2MongoImporter.Builder builder = new Wiki2MongoImporter.Builder();
        
        builder.reader(reader)
               .datastore(new MongoDatastore.Builder()
                                  .client(new MongoClient())
                                  .build())
               .cleaner(new MarkupCleaner.Builder().build());
        
        Wiki2MongoImporter importer = builder.build();
        
        importer.importAll().get();
    }
    
}
