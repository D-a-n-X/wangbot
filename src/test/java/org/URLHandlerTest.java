package org;

import org.junit.jupiter.api.Test;
import wangbot.main.URLHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

class URLHandlerTest {

    @Test
    void convertToFacebed_replacesFacebookDomainWithFacebed() {
        URLHandler urlHandler = new URLHandler();
        String facebookUrl = "https://www.facebook.com/somepage";
        String expected = "https://www.facebed.com/somepage";
        assertEquals(expected, urlHandler.convertToFacebed(facebookUrl));
    }

    @Test
    void convertToFacebed_handlesUrlsWithQueryParameters() {
        URLHandler urlHandler = new URLHandler();
        String facebookUrl = "https://www.facebook.com/story.php?story_fbid=12345&substory_index=0";
        String expected = "https://www.facebed.com/story.php?story_fbid=12345&substory_index=0";
        assertEquals(expected, urlHandler.convertToFacebed(facebookUrl));
    }

    @Test
    void convertToFacebed_handlesUrlsWithoutWwwPrefix() {
        URLHandler urlHandler = new URLHandler();
        String facebookUrl = "https://facebook.com/groups/12345";
        String expected = "https://facebed.com/groups/12345";
        assertEquals(expected, urlHandler.convertToFacebed(facebookUrl));
    }

    @Test
    void convertToFacebed_doesNotModifyNonFacebookUrls() {
        URLHandler urlHandler = new URLHandler();
        String nonFacebookUrl = "https://www.example.com/somepage";
        String expected = "https://www.example.com/somepage";
        assertEquals(expected, urlHandler.convertToFacebed(nonFacebookUrl));
    }
}
