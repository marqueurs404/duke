package core;

import command.*;
import exception.DukeIllegalArgumentException;
import exception.EmptyFieldException;
import exception.InvalidCommandFormatException;
import exception.InvalidIndexException;
import exception.UnknownCommandException;
import task.Deadline;
import task.Task;
import task.Todo;
import task.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

public class Parser {
    /**
     * Parses user input and returns a Command object.
     *
     * @param command User string input.
     * @return executable Command object.
     * @throws EmptyFieldException           If input does not provide a required field value.
     * @throws DukeIllegalArgumentException  If input has invalid field value.
     * @throws InvalidCommandFormatException If input is not in the right format.
     * @throws UnknownCommandException       If a command cannot be understood.
     * @throws InvalidIndexException         If input provides an out-of-bound index.
     */
    public static Command parse(String command) throws EmptyFieldException, DukeIllegalArgumentException,
            InvalidCommandFormatException, UnknownCommandException, InvalidIndexException {
        if (command.equalsIgnoreCase("list")) {
            return new ListTasksCommand(command);
        } else if (command.toLowerCase().startsWith("done")) {
            try {
                int taskIndex = Integer.parseInt(command.split(" ")[1]);
                return new DoneTaskCommand(command, taskIndex);
            } catch (NumberFormatException e) {
                throw new InvalidIndexException("OOPS!!! I can't do that, please gimme a valid task ID mate.");
            }
        } else if (command.toLowerCase().startsWith("todo")) {
            try {
                //parse description from command string
                int commandLength = "todo".length();
                String description = command.substring(commandLength).trim();
                if (description.isEmpty()) {
                    throw new EmptyFieldException("OOPS!!! The description of a task cannot be empty mate.");
                }

                Task task = new Todo(description);

                return new AddTaskCommand(command, task);
            } catch (StringIndexOutOfBoundsException e) {
                throw new InvalidCommandFormatException("OOPS!!! Please gimme a todo with the right format: "
                        + "'todo [description]'");
            }
        } else if (command.toLowerCase().startsWith("deadline")) {
            try {
                int byLength = "/by".length();
                int commandLength = "deadline".length();
                int byIndex = command.indexOf("/by"); // look for '/by' keyword from command

                // parse description from command
                String description = command.substring(commandLength, byIndex).trim();
                if (description.isEmpty()) {
                    throw new EmptyFieldException("OOPS!!! The description of a deadline cannot be empty mate.");
                }

                // parse by-datetime from command
                String by = command.substring(byIndex + byLength).trim();
                if (by.isEmpty()) {
                    throw new EmptyFieldException("OOPS!!! The date of a deadline cannot be empty mate.");
                }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy[ HHmm]");
                TemporalAccessor parsedBy = fmt.parseBest(by, LocalDateTime::from, LocalDate::from);

                Task task = null;

                // create task depending on given whether a by-time is given
                if (parsedBy instanceof LocalDateTime) {
                    task = new Deadline(description, (LocalDateTime) parsedBy);
                } else if (parsedBy instanceof LocalDate) {
                    task = new Deadline(description, ((LocalDate) parsedBy).atStartOfDay(), true);
                }

                assert task != null : "Task is null";

                return new AddTaskCommand(command, task);

            } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                throw new InvalidCommandFormatException("OOPS!!! Please gimme a deadline with the right format: "
                        + "'deadline [description] /by [datetime]'\n\t"
                        + "datetime format: d/M/yyyy[ HHmm]");
            }
        } else if (command.toLowerCase().startsWith("event")) {
            try {
                int atLength = "/at".length();
                int commandLength = "event".length();
                int atIndex = command.indexOf("/at"); // look for '/at' keyword from command

                // parse description from command
                String description = command.substring(commandLength, atIndex).trim();
                if (description.isEmpty()) {
                    throw new EmptyFieldException("OOPS!!! The description of an event cannot be empty mate.");
                }

                // parse at-datetime from command
                String at = command.substring(atIndex + atLength).trim();
                if (at.isEmpty()) {
                    throw new EmptyFieldException("OOPS!!! The start & end dates of an event cannot be empty mate.");
                }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy[ HHmm]");
                DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("[d/M/yyyy][ ][HHmm]");
                Task task = null;
                int dashIndex = at.indexOf("-");
                if (dashIndex != -1) {
                    String atStart = at.substring(0, dashIndex).trim();
                    String atEnd = at.substring(dashIndex + 1).trim();
                    TemporalAccessor parsedStart = fmt.parseBest(atStart, LocalDateTime::from, LocalDate::from);
                    TemporalAccessor parsedEnd = fmt2.parseBest(atEnd,
                            LocalDateTime::from, LocalDate::from, LocalTime::from);
                    if ((parsedStart instanceof LocalDateTime) && (parsedEnd instanceof LocalDateTime)) {
                        task = new Event(description, (LocalDateTime) parsedStart,
                                (LocalDateTime) parsedEnd);
                    } else if ((parsedStart instanceof LocalDate) && ((parsedEnd instanceof LocalDate))) {
                        task = new Event(description, ((LocalDate) parsedStart).atStartOfDay(),
                                ((LocalDate) parsedEnd).atStartOfDay(), true);
                    } else if ((parsedStart instanceof LocalDate) && (parsedEnd instanceof LocalTime)) {
                        task = new Event(description, ((LocalDate) parsedStart).atStartOfDay(),
                                ((LocalTime) parsedEnd).atDate((LocalDate) parsedStart), true);
                    } else {
                        throw new InvalidCommandFormatException(
                                "OOPS!!! Please gimme an event with the right format: \n\t\t"
                                        + "'event [description] /at [start datetime] - [end datetime]'\n\t"
                                        + "datetime format: d/M/yyyy HHmm");
                    }
                } else {
                    TemporalAccessor parsedStart = fmt.parseBest(at, LocalDateTime::from, LocalDate::from);
                    if (parsedStart instanceof LocalDateTime) {
                        task = new Event(description, (LocalDateTime) parsedStart);
                    } else if (parsedStart instanceof LocalDate) {
                        task = new Event(description, ((LocalDate) parsedStart).atStartOfDay(), true);
                    }
                }

                assert task != null : "Task is null";

                return new AddTaskCommand(command, task);

            } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                throw new InvalidCommandFormatException("OOPS!!! Please gimme an event with the right format: \n\t\t"
                        + "'event [description] /at [start datetime] - [end datetime]'\n\t"
                        + "datetime format: d/M/yyyy HHmm");
            }
        } else if (command.toLowerCase().startsWith("delete")) {
            try {
                int taskIndex = Integer.parseInt(command.split(" ")[1]);
                return new DeleteTaskCommand(command, taskIndex);
            } catch (NumberFormatException e) {
                throw new InvalidIndexException("OOPS!!! I can't do that, please gimme a valid task ID mate.");
            }
        } else if (command.toLowerCase().startsWith("find")) {
            try {
                //parse searchString from command string
                int commandLength = "find".length();
                String searchString = command.substring(commandLength).trim();
                if (searchString.isEmpty()) {
                    throw new EmptyFieldException("OOPS!!! Can't search for somethin' with nothin' mate.");
                }

                return new FindTaskCommand(command, searchString);
            } catch (StringIndexOutOfBoundsException e) {
                throw new InvalidCommandFormatException("OOPS!!! Please gimme the right format for searching: "
                        + "'find [keyword]'");
            }
        } else if (command.equalsIgnoreCase("bye")) {
            return new ExitCommand(command);
        } else {
            throw new UnknownCommandException("OOPS!!! Sorry mate, I don't geddit.");
        }
    }
}
