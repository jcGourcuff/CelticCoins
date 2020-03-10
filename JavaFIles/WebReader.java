import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import java.io.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WebReader {

    private static String IMAGE_DESTINATION_FOLDER = "../Images";

    public static void main(String[] args) throws IOException {


      try{

        FileReader fr = new FileReader("webSiteStruct.txt");

        BufferedReader br = new BufferedReader(fr);

        String s;

        String[] arrayWords;

        while((s = br.readLine()) != null) {

          arrayWords = s.split(" ");

          String region = arrayWords[0];

          for(int i =1; i<arrayWords.length; i+=2){

            String tribe_name = arrayWords[i];
            String tribe_code = arrayWords[i+1];
            getImages(tribe_code, region, tribe_name);
          }

        }

        br.close();

        fr.close();

      } catch (Exception e){

          System.err.println("Error: " + e.getMessage());

        }

    }

    private static String downloadImage(String strImageURL, String region, String tribe_name){

        String folder = WebReader.IMAGE_DESTINATION_FOLDER + "/" + tribe_name;
        //get file name from image path
        String strImageName =
                strImageURL.substring( strImageURL.lastIndexOf("/") + 1 );

        String id = strImageName.substring(0, strImageName.lastIndexOf('.'));


        System.out.println("id "+id);
        /*
        System.out.println("Saving: " + strImageName + ", from: " + strImageURL);

        try {

            //open the stream from URL
            URL urlImage = new URL(strImageURL);
            InputStream in = urlImage.openStream();

            byte[] buffer = new byte[4096];
            int n = -1;

            OutputStream os =
                new FileOutputStream( folder + "/" +  strImageName );
            //write bytes to the output stream
            while ( (n = in.read(buffer)) != -1 ){
                os.write(buffer, 0, n);
            }

            //close the stream
            os.close();

            System.out.println("Image saved");





        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return id;

    }

    private static String[] getVanArsdellNumbers(String webURL){

      String list[] = null;
      try{
        Document doc = Jsoup.parse(new URL(webURL), 2000);

        Elements resultLinks = doc.select("a");

        list = new String[resultLinks.size()-14];

        int it = 0;
        for (Element link : resultLinks) {
          //to elimnate other li,ks on web page
          if (it >= 14) {
            String text = link.text();
            if (text.equals("No Van Arsdell Number")){
              text = "";
            }
            list[it-14] = text;
            it ++;
          }
          else{
            it++;
          }
        }

        }catch (IOException e) {
            e.printStackTrace();
        }

      return list;
    }

    private static String[] getInfos(String webURL){
      String list[] = null;
      try{
        Document doc = Jsoup.parse(new URL(webURL),300000);

        Elements resultLinks = doc.select("a[href]");

        list = new String[resultLinks.size()-14];

        int it = 0;
        for (Element link : resultLinks) {
          //to elimnate other li,ks on web page
          if (it >= 14) {

            String url = "http://www.celticcoins.ca/"+link.attr("href");
            Document infoPage = Jsoup.parse(new URL(url), 20000);
            String info = infoPage.body().text();
            String[] sparsed = info.split(" ");
            int n = sparsed.length;
            String metal = null;
            String denomination = null;
            int k =0;
            while(k<n){
              if( sparsed[k].equals("Metal:")){
                String str = new String("");
                while(!sparsed[k+1].equals("Manufacture:")){
                  str = str + sparsed[k+1];
                  k++;
                }
                metal = new String(str);
              }
              if (sparsed[k].equals("Denomination:")){
                String str = new String("");
                while(!sparsed[k+1].equals("Date")){
                  str = str + sparsed[k+1];
                  k++;
                }
                denomination = new String(str);
                break;
              }
              k++;
            }
            String result = new String(metal + " " + denomination);
            list[it-14] = result;
            it ++;
          }
          else{
            it++;
          }
        }

        }catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void getImages(String tribe_code, String region, String tribe_name){
      String url = "http://www.celticcoins.ca/tribe.php?tribe=";
      String[] list_VAnumb = getVanArsdellNumbers(url+tribe_code);
      String subURL = "http://www.celticcoins.ca/vanarsdell.php?tribe=" + tribe_code;
      for(String VAn : list_VAnumb){
         String imagesURL = subURL + "&van=" + VAn;
         try{
         Document document = Jsoup
                 .connect(imagesURL)
                 .userAgent("Mozilla/5.0")
                 .timeout(10 * 1000)
                 .get();

         //select all img tags
         Elements imageElements = document.select("img");

         //iterate over each image

         String[] ids = new String[imageElements.size()];
         int n = 0;
         for(Element imageElement : imageElements){

             //make sure to get the absolute URL using abs: prefix
             String strImageURL = imageElement.attr("abs:src");

             //download image one by one
             String id = downloadImage(strImageURL, region, tribe_name);

             ids[n] = id;
             n++;
         }
         String[] additional_infos = getInfos(imagesURL);
         write_ids(ids, region, tribe_name, additional_infos);
       } catch (IOException e){

           System.err.println("Error: " + e.getMessage());

         }
      }
    }

    private static void write_ids(String[] ids, String Region, String Tribe, String[] infos){

      try{
        FileWriter write = new FileWriter("ids_and_labels.txt", true);
        PrintWriter print_line = new PrintWriter(write);
        int n = ids.length;
        for(int k=0; k<n;k++){
          print_line.printf("%s" + "%n", ids[k] + " "+Region + " "+Tribe + " "+infos[k]);
        }
        print_line.close();
      }catch(IOException e){
        e.printStackTrace();
      }
    }
}
