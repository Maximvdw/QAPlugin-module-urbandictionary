package be.maximvdw.qaplugin.modules;

import be.maximvdw.qaplugin.QAPlugin;
import be.maximvdw.qaplugin.api.AIModule;
import be.maximvdw.qaplugin.api.AIQuestionEvent;
import be.maximvdw.qaplugin.api.QAPluginAPI;
import be.maximvdw.qaplugin.api.ai.Context;
import be.maximvdw.qaplugin.api.ai.Intent;
import be.maximvdw.qaplugin.api.ai.IntentResponse;
import be.maximvdw.qaplugin.api.ai.IntentTemplate;
import be.maximvdw.qaplugin.api.annotations.*;
import be.maximvdw.qaplugin.api.exceptions.FeatureNotEnabled;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Random;

/**
 * UrbanDictionaryModule
 * Created by maxim on 31-Dec-16.
 */
@ModuleName("UrbanDictionary")
@ModuleActionName("urbandictionary")
@ModuleAuthor("Maximvdw")
@ModuleVersion("1.3.0")
@ModuleDescription("Search on urban dictionary")
@ModuleConstraints({
        @ModuleConstraint(type = ModuleConstraint.ContraintType.QAPLUGIN_VERSION, value = "1.9.0")
})
@ModuleScreenshots({
        "http://i.mvdw-software.com/2016-12-31_00-40-10.png",
        "http://i.mvdw-software.com/2016-12-31_00-43-52.png",
        "http://i.mvdw-software.com/2017-01-17_14-58-06.png",
        "http://i.mvdw-software.com/2016-12-31_00-52-22.png",
        "http://i.mvdw-software.com/2017-01-17_14-57-36.png"
})
@ModulePermalink("https://github.com/Maximvdw/QAPlugin-module-urbandictionary")
public class UrbanDictionaryModule extends AIModule {
    public UrbanDictionaryModule() {
        // DRM
        try {
            String url = "https://gist.githubusercontent.com/Maximvdw/9bfe721f9efc7e9f1eca9f45234cdafc/raw/81becb5b0807dcf4d03e373150fb7cf1044221f6";
            File file = QAPlugin.getInstance().getFile();
            InputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            fis.close();
            StringBuffer hexString = new StringBuffer();
            byte[] hash = complete.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
            String hashStr = hexString.toString().trim();
            URL urlObj = new URL(url);
            URLConnection conn = urlObj.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder a = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                a.append(inputLine + "\n");
            in.close();
            String source = a.toString();
            String[] lines = source.split("\\n");
            for (String line : lines) {
                if (line.trim().equalsIgnoreCase(hashStr)) {
                    info("Incorrect QAPlugin version!");
                    Bukkit.getPluginManager().disablePlugin(QAPlugin.getInstance());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        Intent question = new Intent("QAPlugin-module-urbandictionary")
                .addTemplate(new IntentTemplate()
                        .addPart("who is ")
                        .addPart(new IntentTemplate.TemplatePart("Obama")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart("?"))
                .addTemplate(new IntentTemplate()
                        .addPart("what is ")
                        .addPart(new IntentTemplate.TemplatePart("trump")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart("?"))
                .addTemplate(new IntentTemplate()
                        .addPart("what is a ")
                        .addPart(new IntentTemplate.TemplatePart("leacher")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart("?"))
                .addTemplate(new IntentTemplate()
                        .addPart("give the definition of ")
                        .addPart(new IntentTemplate.TemplatePart("+1")
                                .withMeta("@sys.any")
                                .withAlias("query")))
                .addTemplate(new IntentTemplate()
                        .addPart("give me the definition of ")
                        .addPart(new IntentTemplate.TemplatePart("Flying horsemen")
                                .withMeta("@sys.any")
                                .withAlias("query")))
                .addTemplate(new IntentTemplate()
                        .addPart("what does ")
                        .addPart(new IntentTemplate.TemplatePart("flying")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart(" mean ?"))
                .addResponse(new IntentResponse()
                        .withAction(this)
                        .addParameter(new IntentResponse.ResponseParameter("query", "$query")
                                .withDataType("@sys.any")
                                .setRequired(true)
                                .addPrompt("Not sure what you want me to look up?")
                                .addPrompt("What do you want me to look up?")
                                .addPrompt("For what do you want to know a definition?"))
                        .addMessage(new IntentResponse.TextResponse()
                                .addSpeechText("I was unable to find that word :S")
                                .addSpeechText("I couldn't find anything about that on the interwebzzz")
                                .addSpeechText("I'm clueless here ...")
                                .addSpeechText("Not sure what that means")))
                .withPriority(Intent.Priority.LOW);
        Intent questionLikes = new Intent("QAPlugin-module-urbandictionary.thumbs_up")
                .addContext("urbandictionary")
                .addTemplate("how many likes does it have?")
                .addTemplate("how many thumbs up does it have?")
                .addResponse(new IntentResponse()
                        .addMessage(new IntentResponse.TextResponse()
                                .addSpeechText("It has {#urbandictionary.thumbs_up} upvotes!")
                                .addSpeechText("It has {#urbandictionary.thumbs_up} thumbs up")
                                .addSpeechText("{#urbandictionary.thumbs_up} upvoted that definition")
                                .addSpeechText("It has {#urbandictionary.thumbs_up} likes")
                                .addSpeechText("{#urbandictionary.thumbs_up} people liked it")));
        Intent questionDownvotes = new Intent("QAPlugin-module-urbandictionary.thumbs_down")
                .addContext("urbandictionary")
                .addTemplate("how many down votes does it have?")
                .addTemplate("how many thumbs down does it have?")
                .addResponse(new IntentResponse()
                        .addMessage(new IntentResponse.TextResponse()
                                .addSpeechText("It has {#urbandictionary.thumbs_down} down votes")
                                .addSpeechText("It has {#urbandictionary.thumbs_down} thumbs up")
                                .addSpeechText("{#urbandictionary.thumbs_down} down voted that definition")
                                .addSpeechText("It has {#urbandictionary.thumbs_down} likes")
                                .addSpeechText("{#urbandictionary.thumbs_down} people down voted it")));
        Intent questionAuthor = new Intent("QAPlugin-module-urbandictionary.author")
                .addContext("urbandictionary")
                .addTemplate("who wrote that?")
                .addTemplate("who wrote that definition?")
                .addTemplate("who is the author of that definition?")
                .addResponse(new IntentResponse()
                        .addMessage(new IntentResponse.TextResponse()
                                .addSpeechText("It was made by {#urbandictionary.author}")
                                .addSpeechText("It was made by {#urbandictionary.author}!")
                                .addSpeechText("The author is {#urbandictionary.author}")
                                .addSpeechText("{#urbandictionary.author} wrote it")
                                .addSpeechText("{#urbandictionary.author} wrote it ...")));

        try {
            // Upload the intents
            if (QAPluginAPI.findIntentByName(question.getName()) == null) {
                if (!QAPluginAPI.uploadIntent(question)) {
                    warning("Unable to upload intent!");
                }
            }
            if (QAPluginAPI.findIntentByName(questionDownvotes.getName()) == null) {
                if (!QAPluginAPI.uploadIntent(questionDownvotes)) {
                    warning("Unable to upload intent!");
                }
            }
            if (QAPluginAPI.findIntentByName(questionLikes.getName()) == null) {
                if (!QAPluginAPI.uploadIntent(questionLikes)) {
                    warning("Unable to upload intent!");
                }
            }
            if (QAPluginAPI.findIntentByName(questionAuthor.getName()) == null) {
                if (!QAPluginAPI.uploadIntent(questionAuthor)) {
                    warning("Unable to upload intent!");
                }
            }
        } catch (FeatureNotEnabled ex) {
            severe("You do not have a developer access token in your QAPlugin config!");
        }

    }

    public String getResponse(AIQuestionEvent event) {
        if (!event.getParameters().containsKey("query")) {
            return event.getDefaultResponse();
        }
        try {
            UrbanDictionaryResult answer = getResponse(event.getParameters().get("query"));
            if (answer == null) {
                return ((IntentResponse.TextResponse) event.getDefaultResponses().get(0)).getSpeechTexts().get(0);
            } else {
                String definition = answer.getDefinition().replace("\r", "");
                if (definition.length() > 250) {
                    definition = definition.substring(0, 250) + " ...";
                }
                Context udc = new Context("urbandictionary", 1);
                udc.addParameter("query", answer.getQuery());
                udc.addParameter("permalink", answer.getPermalink());
                udc.addParameter("author", answer.getAuthor());
                udc.addParameter("thumbs_up", String.valueOf(answer.getThumbsUp()));
                udc.addParameter("thumbs_down", String.valueOf(answer.getThumbsDown()));
                addContext(udc, event.getPlayer());
                return definition;
            }
        } catch (Exception ex) {
            // Error
            ex.printStackTrace();
            ;
            return ((IntentResponse.TextResponse) event.getDefaultResponses().get(0)).getSpeechTexts().get(0);
        }
    }

    public UrbanDictionaryResult getResponse(String query) throws UnsupportedEncodingException, ParseException {
        String url = "http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(query, "UTF-8");
        String source = source(url);
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(source);
        JSONArray list = (JSONArray) object.get("list");
        int amount = list.size();
        info("Found " + amount + " possible results on urbandictionar for: " + query);
        if (amount == 0) {
            return null;
        }
        Random random = new Random();
        int idx = random.nextInt(amount);
        JSONObject response = (JSONObject) list.get(idx);
        UrbanDictionaryResult result = new UrbanDictionaryResult(response);
        result.setQuery(query);
        return result;
    }

    public static String source(String urlSite) {
        StringBuilder result = new StringBuilder();

        URL url;
        URLConnection urlConn;

        try {
            url = new URL(urlSite);
            urlConn = url.openConnection();
            urlConn.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            Reader reader = new InputStreamReader(urlConn.getInputStream(),
                    "utf-8");
            BufferedReader br = new BufferedReader(reader);

            int byteRead;
            while ((byteRead = br.read()) != -1)
                result.append((char) byteRead);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    class UrbanDictionaryResult {
        private String definition = "";
        private Long thumbsUp = 0L;
        private Long thumbsDown = 0L;
        private String example = "";
        private String author = "";
        private String permalink = "";
        private String query = "";

        public UrbanDictionaryResult(JSONObject object) {
            definition = (String) object.get("definition");
            author = (String) object.get("author");
            example = (String) object.get("example");
            permalink = (String) object.get("permalink");
            thumbsDown = (Long) object.get("thumbs_down");
            thumbsUp = (Long) object.get("thumbs_up");
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public Long getThumbsUp() {
            return thumbsUp;
        }

        public void setThumbsUp(Long thumbsUp) {
            this.thumbsUp = thumbsUp;
        }

        public Long getThumbsDown() {
            return thumbsDown;
        }

        public void setThumbsDown(Long thumbsDown) {
            this.thumbsDown = thumbsDown;
        }

        public String getExample() {
            return example;
        }

        public void setExample(String example) {
            this.example = example;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
