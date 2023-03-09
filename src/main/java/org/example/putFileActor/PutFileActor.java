package org.example.putFileActor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
<<<<<<< HEAD
=======
import org.example.getFileActor.GetFileActor;
>>>>>>> state

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//import static java.awt.TexturePaintContext.getContext;

public class PutFileActor extends EventSourcedBehavior<String, String, PutFileActor.State> {

    private final String outputDir;

<<<<<<< HEAD
=======
    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public PutFileActor.State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }
>>>>>>> state
    public PutFileActor(String outputDir,PersistenceId persistenceId) {
        super(persistenceId);
        this.outputDir = outputDir;
    }

    public static Behavior<String> create(PersistenceId persistenceId,String outputDir) {
        return Behaviors.setup(context -> new PutFileActor(outputDir,persistenceId));
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<String, String , State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(String.class, this::putFile)
                .build();
    }

<<<<<<< HEAD
    private Effect<String, List<String>> putFile(List<String> state, String data){
=======
    private Effect<String, State> putFile(State state, String data) {
>>>>>>> state

        FileWriter fw = null;
        try {
            fw = new FileWriter(outputDir, true);
            fw.write(data);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Effect().persist(data).thenRun(() -> {
            // Log successful persist event
//            getContext().getLog().info("File {} persisted successfully", file);
        });
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
    public SignalHandler<PutFileActor.State> signalHandler() {
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

