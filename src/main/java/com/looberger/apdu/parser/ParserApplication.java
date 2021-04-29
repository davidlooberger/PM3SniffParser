package com.looberger.apdu.parser;

import com.looberger.apdu.parser.util.APDUParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.join;

@SpringBootApplication
public class ParserApplication implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(ParserApplication.class);


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ParserApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

        APDUParser parser = new APDUParser();
        if (args.length >= 1) {
            try (final InputStream is = Files.newInputStream(Paths.get(args[0]))) {
                readFromInput(parser, is);
            } catch (Exception e) {
                System.out.println("Dubious command...");
            }
        } else {
            System.out.println("Reading from STDIN");
            readFromStdIn(parser);
        }
    }

    private void readFromStdIn(APDUParser parser) {
        System.out.println("----- Parser started -------");
        readFromInput(parser, System.in);
    }

    private void readFromInput(APDUParser parser, InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            while (true) {
                if ((line = reader.readLine()) != null) {
                    parser.collect(line);
                } else {
                    parser.dump();
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("Caught exception.", e);
        }
    }
}
