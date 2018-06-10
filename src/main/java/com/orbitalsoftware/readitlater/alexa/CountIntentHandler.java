package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

public class CountIntentHandler implements RequestHandler {

    private static final String KEY_COUNT = "Count";

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("CountIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        AttributesManager mgr = input.getAttributesManager();
        Map<String, Object> persistentAttributes = mgr.getPersistentAttributes();
        BigInteger count = (BigInteger) persistentAttributes.getOrDefault(KEY_COUNT, BigInteger.valueOf(0));
        count.add(BigInteger.valueOf(1));
        persistentAttributes.put(KEY_COUNT, count);
        mgr.savePersistentAttributes();
        String speech = String.format("This intent has been visited %d times.", count.intValue());
        return input.getResponseBuilder()
                .withSpeech(speech)
                .withSimpleCard("HelloWorld", speech)
                .withReprompt("What do you want to do next?")
                .build();
    }
}
