package fr.istic.pdl1819_grp5;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
class ConverterToCsvTest {
   static  Set<UrlMatrix> urlMatrixSet = new HashSet<UrlMatrix>();
   static  WikipediaMatrix wikipediaMatrix = new WikipediaMatrix();
    static String outputDirHtml = "output" + File.separator + "html" + File.separator;
    static String outputDirWikitext = "output" + File.separator + "wikitext" + File.separator;
    static File file = new File("inputdata" + File.separator + "wikiurls.txt");
    static List<String> urls = new ArrayList<String>();

    static  String url;

    /**
     * convert url given in urlMatrix
     * check link wikitext
     * check link html
     * check number of url
     * check url connexion (failure,ok and total)
     * @throws IOException
     *
     */
    @Test
    void convertHtmlTable() throws IOException{


        assertTrue(new File(outputDirHtml).isDirectory());

        assertTrue(new File(outputDirWikitext).isDirectory());
        String BASE_WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/";

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int nbUrlConnectionOk = 0;
        int nbUrlConnectionFailure =0;
        int nbUrlTotal = 0;
        URL uneURL=null;
        while ((url = br.readLine()) != null) {
            String wurl = BASE_WIKIPEDIA_URL + url;
            uneURL = new URL(wurl);
            HttpURLConnection connexion = (HttpURLConnection)uneURL.openConnection();
            if (connexion.getResponseCode() == HttpURLConnection.HTTP_OK){
                urls.add(url);

                // TODO: do something with the Wikipedia URL
                // (ie extract relevant tables for correct URL, with the two extractors)
                // for exporting to CSV files, we will use mkCSVFileName
                // example: for https://en.wikipedia.org/wiki/Comparison_of_operating_system_kernels
                // the *first* extracted table will be exported to a CSV file called
                // "Comparison_of_operating_system_kernels-1.csv"
                // directory where CSV files are exported (HTML extractor)
                urlMatrixSet.add(new UrlMatrix(wurl));
                // the *second* (if any) will be exported to a CSV file called
                // "Comparison_of_operating_system_kernels-2.csv"
                // TODO: the HTML extractor should save CSV files into output/HTML
                // see outputDirHtml
                // TODO: the Wikitext extractor should save CSV files into output/wikitext
                // see outputDirWikitext


                nbUrlConnectionOk++;
            }
            else{
                nbUrlConnectionFailure++;
            }

            }

        nbUrlTotal = nbUrlConnectionOk + nbUrlConnectionFailure;
        assertEquals(nbUrlConnectionFailure, 24,"connection failure");
        assertEquals(nbUrlConnectionOk, 312,"connection ok");
        assertEquals(nbUrlTotal, 336,"connection total");

        wikipediaMatrix.setUrlsMatrix(urlMatrixSet);

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

       // parcoursUrl(ExtractType.HTML,outputDirHtml);

        parcoursUrl(ExtractType.WIKITEXT,outputDirWikitext);



        // directory where CSV files are exported (Wikitext extractor)

        //configuration WikipediaMatrix
        // Change to wikitext if you want this extraction


    }


    /**
     * create filematrix name
     * @param url
     * @param n corresponds to the number of tables
     * @return name filematrix
     */
    static String mkCSVFileName(String url, int n) {
        return url.trim() + "-" + n + ".csv";
    }

    /**
     * create for each urlMatrix a number of fileMatrix
     * @param e corresponds to an extractor type
     * @param directory outputDirHtml
     */
    static void parcoursUrl(ExtractType e,String directory ){
        String csvFileName;
        wikipediaMatrix.setExtractType(e);
        try {
            urlMatrixSet = wikipediaMatrix.getConvertResult();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (UrlMatrix urlMatrix : urlMatrixSet){
            Set<FileMatrix> fileMatrixSet = urlMatrix.getFileMatrix();
            int i=0;
            for (FileMatrix fileMatrix : fileMatrixSet){
                i++;
                String url=urlMatrix.getLink();
                csvFileName=mkCSVFileName(url.substring(url.lastIndexOf("/")+1,url.length()),i);
                try {
                    assertTrue(!fileMatrix.getText().isEmpty());
                    fileMatrix.saveCsv(directory+csvFileName);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * check if wikitext and html have the same number of tables
     * @throws IOException
     */
    @AfterAll
    static void  wikitextVShtml1( ) throws IOException {


       for(String s : urls){
            int html=0, wikitext =0;
           if( (html = nombreOfTable(s, ExtractType.HTML)) != (wikitext=nombreOfTable(s, ExtractType.WIKITEXT))){



               assertTrue(false);
           }
       }


    }

    /**
     * count number of array of an extractor
     * @param title
     * @param e corresponds to an extractor type
     * @return number of tables
     */
    static int  nombreOfTable(String title, ExtractType e){

        String[] files = new File(e==ExtractType.HTML?outputDirHtml:outputDirWikitext).list();

        int nbre = 0;

        for (String s : files){
            if(s.contains(title))
                nbre++;
        }

        return nbre;
    }


    public static String ReadFile(String file){
        String htmltext="";
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(file)));
            String  line=bufferedReader.readLine();
            while (line!=null){
                htmltext+=(line+"\n");
                line=bufferedReader.readLine();
            }
        }catch (FileNotFoundException e){
            System.err.printf("Le fichier n'a pas été trouvé."+ file.toString());
        } catch (IOException e) {
            System.err.printf("Impossible de trouver le fichier."+ file.toString());
        }
        return htmltext;
    }

    @Test
    static void convertTable() throws IOException {

        ConverterToCsv c=new ConverterToCsv();
        Document doc= Jsoup.parse(ReadFile("src/test/1 rowspan/html"));
        Element table = doc.getElementsByTag("table").first();
        FileMatrix fileMatrix=c.convertHtmlTable(table);
        assertTrue(FileUtils.contentEquals(new File("src/test/1 rowspan/csv.csv"),fileMatrix.saveCsv("src/test/1 rowspan/"+fileMatrix.getName()+".csv")));


        doc= Jsoup.parse(ReadFile("src/test/2 simple/html"));
        table = doc.getElementsByTag("table").first();
        ConverterToCsv c2=new ConverterToCsv();
        fileMatrix=c2.convertHtmlTable(table);
        assertTrue(FileUtils.contentEquals(new File("src/test/2 simple/csv.csv"),fileMatrix.saveCsv("src/test/2 simple/"+fileMatrix.getName()+".csv")));


        doc= Jsoup.parse(ReadFile("src/test/3 rowspan/html"));
        table = doc.getElementsByTag("table").first();
        ConverterToCsv c3=new ConverterToCsv();
        fileMatrix=c3.convertHtmlTable(table);
        assertTrue(FileUtils.contentEquals(new File("src/test/3 rowspan/csv.csv"),fileMatrix.saveCsv("src/test/3 rowspan/"+fileMatrix.getName()+".csv")));


        doc= Jsoup.parse(ReadFile("src/test/thead_tfoot/html"));
        table = doc.getElementsByTag("table").first();
        ConverterToCsv c4=new ConverterToCsv();
        fileMatrix=c4.convertHtmlTable(table);
        assertTrue(FileUtils.contentEquals(new File("src/test/thead_tfoot/csv.csv"),fileMatrix.saveCsv("src/test/thead_tfoot/"+fileMatrix.getName()+".csv")));
    }


}
