package org.example.filterFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import akka.persistence.typed.javadsl.Recovery;
import org.example.getFileActor.GetFileActor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterFileActor extends EventSourcedBehavior<String, String, FilterFileActor.State> {

    private final ActorRef<String> putFileActorRef;

    //    final class BookingCompleted implements Event {}                //For Defining Snapshotting policy
    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public FilterFileActor.State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }
    public FilterFileActor(ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        super(persistenceId);
        this.putFileActorRef = putFileActorRef;
    }

    public static Behavior<String> create(ActorRef<String> putFileActorRef,PersistenceId persistenceId) {
        return Behaviors.setup(context -> new FilterFileActor(putFileActorRef,persistenceId));
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<String, String, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::filterFile)
                .build();
    }

    private Effect<String, State> filterFile(State state, String data) {
        String filterData = stringFiltering(data,"Shrey");
        putFileActorRef.tell(filterData);
        return Effect().persist(data).thenRun(() -> {
            // Log successful persist event
//            getContext().getLog().info("File {} persisted successfully", file);
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
    public EventHandler<State, String> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(String.class, (state, event) -> {
                    state.addItem(event);
                    return state;
                })
                .build();
    }

    @Override // override retentionCriteria in EventSourcedBehavior
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(100, 2).withDeleteEventsOnSnapshot();    //Snapshot deletion is triggered after saving a new snapshot.The above example will save snapshots automatically every numberOfEvents = 100. Snapshots that have sequence number less than the sequence number of the saved snapshot minus keepNSnapshots * numberOfEvents (100 * 2) are automatically deleted.Event deletion is triggered after saving a new snapshot. Old events would be deleted prior to old snapshots being deleted.
    }

//    @Override // override shouldSnapshot in EventSourcedBehavior
//    public boolean shouldSnapshot(State state, String event, long sequenceNr) {
//        return event instanceof BookingCompleted;         //BookingCompleted is java class,in which we would specify when to take snapshot
//    }

    @Override
    public SignalHandler<FilterFileActor.State> signalHandler() {
        return newSignalHandlerBuilder()
                .onSignal(
                        SnapshotFailed.class,
                        (state, completed) -> {
                            throw new RuntimeException("TODO: add some on-snapshot-failed side-effect here");
                        })
                .onSignal(
                        DeleteSnapshotsFailed.class,
                        (state, completed) -> {
                            throw new RuntimeException(
                                    "TODO: add some on-delete-snapshot-failed side-effect here");
                        })
                .onSignal(
                        DeleteEventsFailed.class,
                        (state, completed) -> {
                            throw new RuntimeException(
                                    "TODO: add some on-delete-snapshot-failed side-effect here");
                        })
                .build();
    }
    // #retentionCriteriaWithSignals
}

