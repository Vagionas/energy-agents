package uk.ac.eeci;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javatuples.Pair;

import java.io.IOException;
import java.io.Reader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.eeci.Person.Activity;
import uk.ac.eeci.HeterogeneousMarkovChain.MarkovChain;

/**
 * Can be used to read {@link HeterogeneousMarkovChain}s of type {@link Person.Activity}.
 *
 * Contains only class methods.
 */
public abstract class MarkovChainReader {

    private final static String DAY_COLUMN_NAME = "day";
    private final static String TIME_COLUMN_NAME = "time";
    private final static String FROM_ACTIVITY_COLUMN_NAME = "from_activity";
    private final static String TO_ACTIVITY_COLUMN_NAME = "to_activity";
    private final static String PROBABILITY_COLUMN_NAME = "probability";

    /**
     * Reads {@link HeterogeneousMarkovChain}s from a csv input stream.
     *
     * @param reader a reader of input stream
     * @param timeStepSize the time step size of the markov chain
     * @param seed the seed for the markov chain
     * @param timeZone the time zone of the markov chain
     * @return the read {@link HeterogeneousMarkovChain}
     * @throws IOException for all sorts of io issues
     */
    public static HeterogeneousMarkovChain<Activity> readMarkovChainFromFile(Reader reader, Duration timeStepSize,
                                                                             long seed, ZoneId timeZone) throws IOException {
        List<CSVRecord> list = new CSVParser(reader, CSVFormat.DEFAULT).getRecords();
        CSVRecord header = list.get(0);
        if (header.size() != 5) {
            String msg = "Invalid format of markov chain csv. Must have 5 columns.";
            throw new IOException(msg);
        }
        final int dayColumnIndex = MarkovChainReader.columnIndex(header, DAY_COLUMN_NAME);
        final int timeColumnIndex = MarkovChainReader.columnIndex(header, TIME_COLUMN_NAME);
        final int fromActivityColumnIndex = MarkovChainReader.columnIndex(header, FROM_ACTIVITY_COLUMN_NAME);
        final int toActivityColumnIndex = MarkovChainReader.columnIndex(header, TO_ACTIVITY_COLUMN_NAME);
        final int probabilityColumnIndex = MarkovChainReader.columnIndex(header, PROBABILITY_COLUMN_NAME);

        Map<String, Map<LocalTime, MarkovChain<Activity>>> chain = new HashMap<>();
        String[] days = {"weekday", "weekend"};
        for (String day : days) {
            Map<LocalTime, MarkovChain<Activity>> dayChain = new HashMap<>();
            for (LocalTime time : MarkovChainReader.allTimeStampsOfOneDay(timeStepSize)) {
                Map<Pair<Activity, Activity>, Double> probabilities = new HashMap<>();
                list.stream()
                        .filter(csvrecord -> csvrecord.get(dayColumnIndex).equals(day))
                        .filter(csvrecord -> LocalTime.parse(csvrecord.get(timeColumnIndex), DateTimeFormatter.ISO_LOCAL_TIME).equals(time))
                        .forEach(csvrecord -> probabilities.put(new Pair<>(Activity.valueOf(csvrecord.get(fromActivityColumnIndex)),
                                                                           Activity.valueOf(csvrecord.get(toActivityColumnIndex))),
                                                                Double.valueOf(csvrecord.get(probabilityColumnIndex))));
                dayChain.put(time, new HeterogeneousMarkovChain.MarkovChain<>(probabilities, seed));
            }
            chain.put(day, dayChain);
        }
        return new HeterogeneousMarkovChain<>(chain.get("weekday"), chain.get("weekend"), timeZone);
    }

    private static int columnIndex(CSVRecord header, String columnName) throws IOException {
        for (int i = 0; i < 5; i++) {
            if (header.get(i).equals(columnName)) {
                return i;
            }
        }
        throw new IOException(String.format("Column name %s not in header.", columnName));
    }

    private static List<LocalTime> allTimeStampsOfOneDay(Duration timeStepSize) {
        List<LocalTime> list = new LinkedList<>();
        LocalDate date = LocalDate.of(2017, 1, 1); // arbitrary date
        LocalDateTime tsp = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        do {
            list.add(tsp.toLocalTime());
            tsp = tsp.plus(timeStepSize);
        } while (date.equals(tsp.toLocalDate()));
        return list;
    }
}