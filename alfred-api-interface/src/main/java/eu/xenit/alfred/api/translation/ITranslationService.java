package eu.xenit.alfred.api.translation;

import java.util.Locale;

/**
 * Service that provides translations
 */
public interface ITranslationService {

    /**
     * Gets the checksum of the current translations of the given language. Can be used for fast checking whether the
     * translations changed or not.
     *
     * @param locale the language of which the checksum of the translations is requested.
     * @return The checksum of the current translations of the given language.
     */
    long getTranslationsCheckSum(Locale locale);

    /**
     * Gets the translations of the given language (locale).
     *
     * @param locale The language of which the translations are requested.
     * @return The translations of the given language.
     */
    Translations getTranslations(Locale locale);

    /**
     * Gets the translated message for the current locale for the provided message ID.
     *
     * @param message The ID of the message to translate.
     * @return The translated String message of the current set language.
     */
    String getMessageTranslation(String message);

    /**
     * Gets the translated message for the provided locale for the provided message ID.
     *
     * @param message The ID of the message to translate.
     * @param locale The ID of the target locale to translate to
     * @return The translated String message of the provided locale.
     */
    String getMessageTranslation(String message, Locale locale);
}
