import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

// Please put application constants or config variables here.
public class Definitions {
    public static final String seedFN = "./txt/seed.txt";
    public static final String visitedFN = "./txt/visited.txt";
    public static final String toVisitFN = "./txt/to_visit.txt";
    public static final String visitedHostsFN = "./txt/visitedHosts.txt";
    //public  static  final MongoDatabase database = MongoClient.getDatabase("try");
    public static final String HTML_DLD_PATH = "./download/";
}
