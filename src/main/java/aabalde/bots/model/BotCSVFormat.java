package aabalde.bots.model;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by aabalde on 22/03/18.
 */
public class BotCSVFormat {

    public static CSVFormat csvFormat = CSVFormat.RFC4180
            .withDelimiter(',')
            .withQuote('"')
            .withRecordSeparator("\n")
            .withIgnoreEmptyLines(false)
            .withQuoteMode(QuoteMode.NON_NUMERIC);

    /**
     * Method for printing the record to the CSV file
     *
     * @param appendable    The appendable where the CSV record will be appended
     * @param values    The objects to print. Must have length 5. From 1st to 4th must be strings, and 5th must be
     *                  a long.
     */
    public static void printCSVRecord(Appendable appendable, final Object... values) throws IOException {
        if(values.length != 4) throw new IllegalArgumentException("Wrong number of values to print the CSV record");

        String list = (String)values[0];
        String desc = (String)values[1];
        String category = (String)values[2];
        String user = (String)values[3];

        BotCSVFormat.csvFormat.printRecord(appendable,
                list,
                desc,
                category,
                user
        );
    }

    public static List<CSVRecord> readCSVRecord(FileReader in) throws Exception{
        CSVParser parser = csvFormat.parse(in);
        return parser.getRecords();
    }

}
