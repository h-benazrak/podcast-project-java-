package model;

import java.util.*;
import java.time.LocalDateTime;
import java.io.*;

public class EpisodeRepository {

    private List<Episode> episodes = new ArrayList<>();
    private final String FILE = "episodes.txt";

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public Episode createEpisode(String type, String title, int duration) {

        String id = "EP" + (episodes.size() + 1);
        Episode ep;

        if (type.equalsIgnoreCase("Regular")) {
            ep = new RegularEpisode(id, title, duration);
        } else {
            ep = new BonusEpisode(id, title, duration);
        }

        episodes.add(ep);
        return ep;
    }


    public void scheduleEpisode(Episode ep, LocalDateTime dt)
            throws ScheduleConflictException {

        for (Episode e : episodes) {
            if (e.getScheduledDateTime() != null && e.getScheduledDateTime().equals(dt)) {
                throw new ScheduleConflictException("There is an episode in the same date please change it !!");
            }
        }
        ep.schedule(dt);
    }

    public void saveToFile() throws EpisodePersistenceException {

        if (episodes.isEmpty()) {
            System.out.println("No episodes to save");
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {

            for (Episode e : episodes) {
                pw.println(
                        e.getId() + "|" + e.getTypeLabel() + "|" + e.getTitle() + "|" +
                                e.getDurationMinutes() + "|" + e.getStatus() + "|" + e.getScheduledDateTime()
                );
            }

        } catch (IOException ex) {
            throw new EpisodePersistenceException("Error saving file", ex);
        }
    }


    public void loadFromFile() throws EpisodePersistenceException {
        episodes.clear();
        File f = new File(FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");

                Episode ep;
                if (p[1].equals("Regular")) {
                    ep = new RegularEpisode(p[0], p[2], Integer.parseInt(p[3]));
                } else {
                    ep = new BonusEpisode(p[0], p[2], Integer.parseInt(p[3]));
                }

                ep.setStatus(EpisodeStatus.valueOf(p[4]));
                if (!p[5].equals("null")) {
                    ep.setScheduledDateTime(LocalDateTime.parse(p[5]));
                }

                episodes.add(ep);
            }

        } catch (IOException ex) {
            throw new EpisodePersistenceException("Error loading file", ex);
        }
    }
}
