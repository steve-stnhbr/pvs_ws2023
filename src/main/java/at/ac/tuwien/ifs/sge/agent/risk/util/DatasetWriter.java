package at.ac.tuwien.ifs.sge.agent.risk.util;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatasetWriter {
  public static class CSV {
    private static CSV instance;

    private final PrintWriter writer;

    private CSV(String fileName) {
      try {
        writer = new PrintWriter(new FileWriter(fileName));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public void appendToCSV(String gameState, float... scalarValue) {
      store(gameState, scalarValue);
    }

    public void appendToCSV(String str) {
      write(str);
    }

    private void store(String gameState, float[] scalarValue) {
      String scalarValueString = IntStream.range(0, scalarValue.length)
              .mapToObj(i -> String.valueOf(scalarValue[i]))
              .collect(Collectors.joining(":"));
      write(gameState + "," + scalarValueString);
    }

    private void write(String s) {
      writer.println(s);
      writer.flush();
    }


    public static CSV getInstance() {
      if (instance == null)
        instance = new CSV("out/data.csv");
      return instance;
    }

  }
  /*
  public static class MongoDB {
    private static final String SERVER_URL = "mongodb://217.160.218.60:27017";
    private static MongoDB instance;

    private final MongoClient client;
    private final MongoDatabase db;
    private final MongoCollection<Document> collection;

    private MongoDB() {
      System.out.println("Connecting to " + SERVER_URL);
      client = MongoClients.create(SERVER_URL);
      System.out.println("Connection successfull:" + preFlightChecks(client));
      db = client.getDatabase("pvs");
      collection = db.getCollection("pvs");
    }

    private static boolean preFlightChecks(MongoClient mongoClient) {
      Document pingCommand = new Document("ping", 1);
      Document response = mongoClient.getDatabase("admin").runCommand(pingCommand);
      System.out.println("=> Print result of the '{ping: 1}' command.");
      return response.getDouble("ok").equals(1.0);
    }

    public void store(INDArray gameState, float... values) {
      float[] gameStateArray = gameState.data().asFloat();
      Document doc = new Document();
      doc.put("state", gameStateArray);
      doc.put("values", values);
      collection.insertOne(doc);
    }

    public void store(Document gameState, float... values) {
      System.out.println("Storing " + gameState);
      Document doc = new Document();
      doc.put("state", gameState);
      doc.put("values", IntStream.range(0, values.length)
              .mapToObj(i -> new BsonDouble(values[i]))
              .collect(Collectors.toList()));
      collection.insertOne(doc);
    }

    public void storeAll(List<Triple<Document, Float, Integer>> triples, float... values) {
      System.out.printf("Storing %d states%n", triples.size());
      List<Document> documents = triples.stream()
              .map(t -> {
                Document doc = new Document();
                doc.put("state", t.getA());
                doc.put("values", IntStream.range(0, values.length + 2)
                        .mapToObj(i -> {
                          if (i == values.length) {
                            return t.getB();
                          } else if (i == values.length + 1) {
                            return (float) t.getC();
                          } else {
                            return values[i];
                          }
                        })
                        .map(BsonDouble::new)
                        .collect(Collectors.toList()));
                return doc;
              })
              .collect(Collectors.toList());

      collection.insertMany(documents);
    }

    public static MongoDB getInstance() {
      if (instance == null) {
        instance = new MongoDB();
      }
      return instance;
    }
  }
   */
}
