package com.deo.cmrefresher;

/**
 *
 * @author deo
 */
import java.util.List;

public interface FeedParser {
    List<Message> parse();
}