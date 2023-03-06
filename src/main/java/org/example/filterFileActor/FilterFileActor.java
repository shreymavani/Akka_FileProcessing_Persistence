package org.example.filterFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import akka.persistence.typed.javadsl.Recovery;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterFileActor extends EventSourcedBehavior<String, String, List<String>> {

    private final ActorRef<String> putFileActorRef;

    public FilterFileActor(ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        super(persistenceId);
        this.putFileActorRef = putFileActorRef;
    }

    public static Behavior<String> create(ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        return Behaviors.setup(context -> new FilterFileActor(putFileActorRef,persistenceId));
    }

    @Override
    public List<String> emptyState() {
        return new ArrayList<String>();
    }

    @Override
    public CommandHandler<String, String, List<String>> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::filterFile)
                .build();
    }

    private Effect<String, List<String>> filterFile(List<String> state, String data) {
        String filterData = stringFiltering(data,"Shrey");
        putFileActorRef.tell(filterData);
        return Effect().persist(filterData).thenRun(() -> {
            // Log successful persist event
//            getContext().getLog().info("File {} persisted successfully", filterData);
        });
    }

    public String stringFiltering(String inputData, String regex) {
        String[] splitData = inputData.split("\n");
        String filterData = "";
//        System.out.println(splitData.length);
        for (String line : splitData) {

            Pattern pattern = Pattern.compile(regex);            // Compile the pattern

            Matcher matcher = pattern.matcher(line);             // Replace the password with the string ""

            filterData += (matcher.replaceAll("Kuldeep") + "\n");     // Replace the word "password" and password details with the string ""
        }

        return filterData;

    }

    @Override
    public EventHandler<List<String>, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(String.class, (state, event) -> {
                    state.add(event);
                    return state;
                })
                .build();
    }

//    public Recovery recovery() {
//        return Recovery.withSnapshotSelectionCriteria(SnapshotSelectionCriteria.none());
//    }
}

