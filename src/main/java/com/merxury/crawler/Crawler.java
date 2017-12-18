package com.merxury.crawler;

import com.merxury.controller.Controller;
import com.merxury.persistent.FileHelper;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.regex.Pattern;

public class Crawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz|about))$");
    private final static int HAS_NO_SUBCATEGORY_LENGTH = 7;
    private final static int HAS_SUBCATEGORY_LENGTH = 8;
    private final static String TAG_SELECTOR = "";
    private final static FileHelper fileHelper = FileHelper.getInstance(Controller.crawlStorageFolder);
    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.contains("edition.cnn.com");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL().replace("http://", "");
        System.out.println("URL: " + url);
        String[] urlKeywords = url.split("/");
        if(urlKeywords.length < 7) {
            return;
        }
        String websiteAddress = urlKeywords[0];
        String year = urlKeywords[1];
        String month = urlKeywords[2];
        String day = urlKeywords[3];
        String category = urlKeywords[4];
        String subCategory = urlKeywords[urlKeywords.length - 3];
        String newsTitle = urlKeywords[urlKeywords.length - 2];


        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();

            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByClass("l-container");
            String content = elements.text();
            if(content.length() == 0) {
                return;
            }
            if(urlKeywords.length == HAS_NO_SUBCATEGORY_LENGTH) {
                fileHelper.save(content,category);
            }
            if(urlKeywords.length == HAS_SUBCATEGORY_LENGTH) {
                fileHelper.save(content,newsTitle, subCategory);
            }
            System.out.println(content);
        }
    }
}
