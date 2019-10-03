/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.utils.command.argparser;

import baritone.api.utils.command.argument.CommandArgument;
import baritone.api.utils.command.exception.CommandInvalidTypeException;
import baritone.api.utils.command.exception.CommandNoParserForTypeException;
import baritone.api.utils.command.exception.CommandUnhandledException;
import baritone.api.utils.command.registry.Registry;

public class ArgParserManager {

    public static final Registry<IArgParser> REGISTRY = new Registry<>();

    static {
        DefaultArgParsers.ALL.forEach(REGISTRY::register);
    }

    /**
     * @param type The type trying to be parsed
     * @return A parser that can parse arguments into this class, if found.
     */
    public static <T> IArgParser.Stateless<T> getParserStateless(Class<T> type) {
        //noinspection unchecked
        return REGISTRY.descendingStream()
                .filter(IArgParser.Stateless.class::isInstance)
                .map(IArgParser.Stateless.class::cast)
                .filter(parser -> parser.getTarget().isAssignableFrom(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * @param type The type trying to be parsed
     * @return A parser that can parse arguments into this class, if found.
     */
    public static <T, S> IArgParser.Stated<T, S> getParserStated(Class<T> type, Class<S> stateKlass) {
        //noinspection unchecked
        return REGISTRY.descendingStream()
                .filter(IArgParser.Stated.class::isInstance)
                .map(IArgParser.Stated.class::cast)
                .filter(parser -> parser.getTarget().isAssignableFrom(type))
                .filter(parser -> parser.getStateType().isAssignableFrom(stateKlass))
                .map(IArgParser.Stated.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Attempt to parse the specified argument with a stateless {@link IArgParser} that outputs the specified class.
     *
     * @param type  The type to try and parse the argument into.
     * @param arg   The argument to parse.
     * @return An instance of the specified class.
     * @throws CommandInvalidTypeException If the parsing failed
     */
    public static <T> T parseStateless(Class<T> type, CommandArgument arg) throws CommandInvalidTypeException {
        IArgParser.Stateless<T> parser = getParserStateless(type);
        if (parser == null) {
            throw new CommandNoParserForTypeException(type);
        }
        try {
            return parser.parseArg(arg);
        } catch (Exception exc) {
            throw new CommandInvalidTypeException(arg, type.getSimpleName());
        }
    }

    /**
     * Attempt to parse the specified argument with a stated {@link IArgParser} that outputs the specified class.
     *
     * @param type  The type to try and parse the argument into.
     * @param arg   The argument to parse.
     * @param state The state to pass to the {@link IArgParser.Stated}.
     * @return An instance of the specified class.
     * @throws CommandInvalidTypeException If the parsing failed
     * @see IArgParser.Stated
     */
    public static <T, S> T parseStated(Class<T> type, Class<S> stateKlass, CommandArgument arg, S state) throws CommandInvalidTypeException {
        IArgParser.Stated<T, S> parser = getParserStated(type, stateKlass);
        if (parser == null) {
            throw new CommandNoParserForTypeException(type);
        }
        try {
            return parser.parseArg(arg, state);
        } catch (Exception exc) {
            throw new CommandInvalidTypeException(arg, type.getSimpleName());
        }
    }
}
