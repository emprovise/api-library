package com.emprovise.api.google.search.datatype;

/**
 * https://developers.google.com/custom-search/docs/ref_languages
 */
public enum SearchLanguage {

    Arabic("lang_ar"),
    Bulgarian("lang_bg"),
    Catalan("lang_ca"),
    Croatian("lang_hr"),
    Chinese_Simplified("lang_zh-cn"),
    Chinese_Traditional("lang_zh-tw"),
    Czech("lang_cs"),
    Danish("lang_da"),
    Dutch("lang_nl"),
    English("lang_en"),
    Estonian("lang_et"),
    Finnish("lang_fi"),
    French("lang_fr"),
    German("lang_de"),
    Greek("lang_el"),
    Hebrew("lang_iw"),
    Hungarian("lang_hu"),
    Icelandic("lang_is"),
    Indonesian("lang_id"),
    Italian("lang_it"),
    Japanese("lang_ja"),
    Korean("lang_ko"),
    Latvian("lang_lv"),
    Lithuanian("lang_lt"),
    Norwegian("lang_no"),
    Polish("lang_pl"),
    Portuguese("lang_pt"),
    Romanian("lang_ro"),
    Russian("lang_ru"),
    Serbian("lang_sr"),
    Slovak("lang_sk"),
    Slovenian("lang_sl"),
    Spanish("lang_es"),
    Swedish("lang_sv"),
    Turkish("lang_tr");

    private String language;

    SearchLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }
}
