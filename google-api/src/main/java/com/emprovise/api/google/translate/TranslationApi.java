package com.emprovise.api.google.translate;

import com.emprovise.api.google.translate.datatype.Language;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.LanguagesListResponse;
import com.google.api.services.translate.model.LanguagesResource;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *  Google Cloud Translation API is a paid service.
 */
public class TranslationApi {

    private Translate translator;

    public TranslationApi(String application, String apiKey) {

        final TranslateRequestInitializer keyInitializer = new TranslateRequestInitializer(apiKey);

        // Set up the HTTP transport and JSON factory
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        // Set up translate
        this.translator = new Translate.Builder(httpTransport, jsonFactory, null)
                                        .setApplicationName(application)
                                        .setTranslateRequestInitializer(keyInitializer)
                                        .build();
    }

    // output: {"languages":[{"language":"af"},{"language":"ar"},{"language":"az"},{"language":"be"},{"language":"bg"},{"language":"bn"},{"language":"bs"},{"language":"ca"},{"language":"ceb"},{"language":"cs"},{"language":"cy"},{"language":"da"},{"language":"de"},{"language":"el"},{"language":"en"},{"language":"eo"},{"language":"es"},{"language":"et"},{"language":"eu"},{"language":"fa"},{"language":"fi"},{"language":"fr"},{"language":"ga"},{"language":"gl"},{"language":"gu"},{"language":"ha"},{"language":"hi"},{"language":"hmn"},{"language":"hr"},{"language":"ht"},{"language":"hu"},{"language":"hy"},{"language":"id"},{"language":"ig"},{"language":"is"},{"language":"it"},{"language":"iw"},{"language":"ja"},{"language":"jw"},{"language":"ka"},{"language":"kk"},{"language":"km"},{"language":"kn"},{"language":"ko"},{"language":"la"},{"language":"lo"},{"language":"lt"},{"language":"lv"},{"language":"mg"},{"language":"mi"},{"language":"mk"},{"language":"ml"},{"language":"mn"},{"language":"mr"},{"language":"ms"},{"language":"mt"},{"language":"my"},{"language":"ne"},{"language":"nl"},{"language":"no"},{"language":"ny"},{"language":"pa"},{"language":"pl"},{"language":"pt"},{"language":"ro"},{"language":"ru"},{"language":"si"},{"language":"sk"},{"language":"sl"},{"language":"so"},{"language":"sq"},{"language":"sr"},{"language":"st"},{"language":"su"},{"language":"sv"},{"language":"sw"},{"language":"ta"},{"language":"te"},{"language":"tg"},{"language":"th"},{"language":"tl"},{"language":"tr"},{"language":"uk"},{"language":"ur"},{"language":"uz"},{"language":"vi"},{"language":"yi"},{"language":"yo"},{"language":"zh"},{"language":"zh-TW"},{"language":"zu"}]}
    public List<LanguagesResource> getLanguages() throws IOException {
        LanguagesListResponse languagesListResponse = translator.languages().list().execute();
        return languagesListResponse.getLanguages();
    }

    // output: {"translations":[{"detectedSourceLanguage":"en","translatedText":"Bonjour le monde"},{"detectedSourceLanguage":"en","translatedText":"Où puis-je promener mon chien"}]}
    public List<TranslationsResource> translate(Language language, String... phrase) throws IOException {
        TranslationsListResponse translationsListResponse = translator.translations().list(Arrays.asList(phrase), language.getLanguage()).execute();
        return translationsListResponse.getTranslations();
    }
}
