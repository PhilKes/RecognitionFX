package options;

import java.util.HashMap;

/**
 * Defintion for default Tesseract OCR settings*/
public class TesseractConstants {
    public static final String WHITELIST="tessedit_char_whitelist",
                            LANGUAGE="lang",
                            TESSDATA="tessdata";
    public static final String[] LANGUAGES={"deu","eng","osd"};
    public static class DEFAULTS {
        private static HashMap<String,String> tesseractOptions;
        static {
            tesseractOptions=new HashMap<>();
            tesseractOptions.put(TesseractConstants.WHITELIST, "ABCDEFGHIKLMNOPQRSTUVWXYZÄÖÜabcdefghiklmnopqrstuvwxyzäüö.:-+0123456789");
            tesseractOptions.put(TesseractConstants.LANGUAGE, TesseractConstants.LANGUAGES[0]);
            tesseractOptions.put(TesseractConstants.TESSDATA, "F:\\Programme\\Tesseract-OCR\\tessdata");
        }
        public static String get(String key){
            if(tesseractOptions.containsKey(key))
                return tesseractOptions.get(key);
            return null;
        }
        public static HashMap<String,String> getAll(){
            return tesseractOptions;
        }
    }
}
