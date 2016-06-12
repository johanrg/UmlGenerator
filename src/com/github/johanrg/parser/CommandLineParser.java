package com.github.johanrg.parser;

import com.sun.javaws.exceptions.InvalidArgumentException;
import sun.tools.jar.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Johan Gustafsson
 * @created 6/2/2016.
 */

public class CommandLineParser {
    private final String[] args;

    public CommandLineParser(String[] args) {
        this.args = args;
    }

    private int findSwitch(String commandSwitch) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals(commandSwitch)) {
                return i;
            }
        }

        return -1;
    }

    public int getInteger(String commandSwitch, int defaultValue) throws CommandLineParserException {
        int i = findSwitch(commandSwitch);

        if (i != -1) {
            if (i + 1 < args.length) {
                ++i;
                try {
                    return Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {
                    throw new CommandLineParserException(String.format("%s must have an integer value.", commandSwitch));
                }
            } else {
                throw new CommandLineParserException(String.format("%s did not have a value", commandSwitch));
            }
        }
        return defaultValue;
    }

    public String getString(String commandSwitch, String defaultValue, boolean restOfTheArgs) throws CommandLineParserException {
        int i = findSwitch(commandSwitch);

        if (i != -1) {
            if (i + 1 < args.length) {
                ++i;
                StringBuilder sb = new StringBuilder();
                String delim = " ";
                while (i < args.length) {
                    sb.append(args[i]).append(delim).append(" ");
                    delim = " ";
                    ++i;

                    if (!restOfTheArgs) {
                        break;
                    }
                }

                if (sb.length() == 0) {
                    throw new CommandLineParserException(String.format("%s did not have a value", commandSwitch));
                }
                return sb.toString();

            }
        }

        return defaultValue;
    }

    public List<String> getList(String commandSwitch, int maxNumberOfItems) throws CommandLineParserException {
        int i = findSwitch(commandSwitch);
        List<String> list = new ArrayList<>();

        if (i != -1) {
            if (i + 1 < args.length) {
                ++i;
                while (i < args.length && (maxNumberOfItems > 0 || maxNumberOfItems == -1)) {
                    list.add(args[i]);
                    ++i;
                    if (maxNumberOfItems != -1) {
                        --maxNumberOfItems;
                    }
                }
            }
        } else {
            throw new CommandLineParserException(String.format("Expected the parameter %s", commandSwitch));
        }
        if (list.size() == 0) {
            throw new CommandLineParserException(String.format("%s did not have a value", commandSwitch));
        } else {
            return list;
        }
    }

    /**
     * Find a single parameter that holds no value
     *
     * @param commandSwitch the argument to find from the args
     * @return boolean value true if available, else false.
     */
    public boolean getParam(String commandSwitch) {
        for (String arg : args) {
            if (arg.equals(commandSwitch)) {
                return true;
            }
        }

        return false;
    }
}
