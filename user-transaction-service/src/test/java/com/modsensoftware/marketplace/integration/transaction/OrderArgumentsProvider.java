package com.modsensoftware.marketplace.integration.transaction;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/30/2022
 */
public class OrderArgumentsProvider implements ArgumentsProvider {

    protected static String noPositionVersionProvidedMessage;
    protected static String insufficientItemsInStockMessage;
    protected static String insufficientOrderAmountMessage;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(999L, 6.0, null, format(noPositionVersionProvidedMessage, 999L)),
                Arguments.of(999L, 100000.0, 0L, format(insufficientItemsInStockMessage, 999L, 100000.0, 150.0)),
                Arguments.of(999L, 1.0, 0L, format(insufficientOrderAmountMessage, 1.0, 999L, 5.0))
        );
    }
}
