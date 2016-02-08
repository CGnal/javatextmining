import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.net.URL;

public class EnglishNormalChunker {

    public static void main(String[] arggg) throws Exception {

        Config config = ConfigFactory.load();

        String model = config.getString("englishModel");

        URL resource = EnglishNormalChunker.class.getResource(model);

        if(resource==null){
            System.out.println("cannot load english model!");
            System.exit(-1);
        }


        String path = resource.toURI().getPath();
        System.out.println("Reading chunker from file default english");

        Chunker chunker
                = (Chunker) AbstractExternalizable.readObject(new File(path));

        String[] args ="Bob Marley lives in Wisconsin".split(" ");


        for (int i = 0; i < args.length; ++i) {
            Chunking chunking = chunker.chunk(args[i]);
            System.out.println("Chunking=" + chunking);
        }

    }

}
