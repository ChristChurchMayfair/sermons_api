package org.christchurchmayfair.api.sermons.persistence;

import org.christchurchmayfair.api.sermons.model.Event;
import org.christchurchmayfair.api.sermons.model.Series;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.christchurchmayfair.api.sermons.model.Speaker;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SermonDataStore {
  List<Sermon> getSermons() throws IOException;

  List<Series> getSerieses() throws IOException;

  List<Speaker> getSpeakers() throws IOException;

  List<Event> getEvents() throws IOException;

  Optional<String> createSermon(Sermon newSermon) throws Exception;
}
