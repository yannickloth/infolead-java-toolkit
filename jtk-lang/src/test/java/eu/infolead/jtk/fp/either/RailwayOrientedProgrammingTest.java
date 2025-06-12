package eu.infolead.jtk.fp.either;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import eu.infolead.jtk.fp.Fn;
import eu.infolead.jtk.fp.Mapper;

/**
 * Comprehensive tests for Railway Oriented Programming operations in Either hierarchy.
 * 
 * <h2>Railway Metaphor</h2>
 * In Railway-Oriented Programming:
 * <ul>
 *   <li><strong>Success Track (Right)</strong> - The main railway line where everything goes well</li>
 *   <li><strong>Error Track (Left)</strong> - The side track where failures are handled</li>
 *   <li><strong>Switch Functions</strong> - Operations that can derail from success to error track</li>
 *   <li><strong>Bypass Functions</strong> - Operations that work on both tracks simultaneously</li>
 *   <li><strong>Track Joining</strong> - Combining multiple railway lines</li>
 *   <li><strong>Track Switching</strong> - Moving between success and error tracks</li>
 * </ul>
 */
class RailwayOrientedProgrammingTest {

    // ====================== BYPASS BOTH TRACKS TESTS (BIMAP) ======================

    @Test
    void testBypassBothTracks_WhenOnSuccessTrack() {
        // Arrange: Train is on the success track
        Either<String, Integer> onSuccessTrack = Either.right(5);
        
        // Act: Apply bypass function to both tracks simultaneously  
        Either<String, String> result = onSuccessTrack.bimap(
            errorTrackData -> "Error Track: " + errorTrackData,
            successTrackData -> "Success Track: " + successTrackData
        );
        
        // Assert: Train stays on success track with transformed data
        assertTrue(result.isRight().toBoolean());
        assertEquals("Success Track: 5", result.orNull());
    }

    @Test
    void testBypassBothTracks_WhenOnErrorTrack() {
        // Arrange: Train is on the error track
        Either<String, Integer> onErrorTrack = Either.left("failure");
        
        // Act: Apply bypass function to both tracks simultaneously
        Either<String, String> result = onErrorTrack.bimap(
            errorTrackData -> "Error Track: " + errorTrackData,
            successTrackData -> "Success Track: " + successTrackData
        );
        
        // Assert: Train stays on error track with transformed data
        assertTrue(result.isLeft().toBoolean());
        assertEquals("Error Track: failure", result.fold(e -> e, v -> null));
    }

    // ====================== TRACK JUNCTION TESTS (APPLICATIVE) ======================

    @Test
    void testTrackJunction_BothTrainsOnSuccessTrack() {
        // Arrange: One train carries data, another carries processing function
        Either<String, Integer> dataTrail = Either.right(5);
        Mapper<String, ? super Integer> processor = i -> "Processed: " + i;
        Either<String, Mapper<String, ? super Integer>> functionTrain = Either.right(processor);
        
        // Act: Apply function from second train to data from first train
        Either<String, String> result = dataTrail.apply(functionTrain);
        
        // Assert: Both trains successfully meet at junction, data gets processed
        assertTrue(result.isRight().toBoolean());
        assertEquals("Processed: 5", result.orNull());
    }

    @Test
    void testTrackJunction_FunctionTrainDerailed() {
        // Arrange: Data train on success track, but function train derailed
        Either<String, Integer> dataTrail = Either.right(5);
        Either<String, Mapper<String, ? super Integer>> functionTrain = Either.left("processor malfunction");
        
        // Act: Try to apply function at junction
        Either<String, String> result = dataTrail.apply(functionTrain);
        
        // Assert: Junction fails because function train derailed
        assertTrue(result.isLeft().toBoolean());
        assertEquals("processor malfunction", result.fold(e -> e, v -> null));
    }

    @Test
    void testTrackJunction_DataTrainDerailed() {
        // Arrange: Function train fine, but data train derailed
        Either<String, Integer> dataTrail = Either.left("data corruption");
        Mapper<String, ? super Integer> processor = i -> "Processed: " + i;
        Either<String, Mapper<String, ? super Integer>> functionTrain = Either.right(processor);
        
        // Act: Try to apply function at junction
        Either<String, String> result = dataTrail.apply(functionTrain);
        
        // Assert: Junction fails because data train derailed
        assertTrue(result.isLeft().toBoolean());
        assertEquals("data corruption", result.fold(e -> e, v -> null));
    }

    @Test
    void testTwoTrainMerge_BothOnSuccessTrack() {
        // Arrange: Two cargo trains successfully carrying data
        Either<String, Integer> cargoTrain1 = Either.right(3);
        Either<String, Integer> cargoTrain2 = Either.right(4);
        
        // Act: Merge cargo from both trains at railway junction
        Either<String, Integer> result = cargoTrain1.map2(cargoTrain2, Integer::sum);
        
        // Assert: Successfully combined cargo continues on success track
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(7), result.orNull());
    }

    @Test
    void testTwoTrainMerge_FirstTrainDerailed() {
        // Arrange: First train derailed, second train fine
        Either<String, Integer> derailedTrain = Either.left("engine failure");
        Either<String, Integer> workingTrain = Either.right(4);
        
        // Act: Try to merge at junction
        Either<String, Integer> result = derailedTrain.map2(workingTrain, Integer::sum);
        
        // Assert: Merge fails, error from first derailment propagates
        assertTrue(result.isLeft().toBoolean());
        assertEquals("engine failure", result.fold(e -> e, v -> null));
    }

    @Test
    void testTwoTrainMerge_SecondTrainDerailed() {
        // Arrange: First train fine, second train derailed
        Either<String, Integer> workingTrain = Either.right(3);
        Either<String, Integer> derailedTrain = Either.left("brake failure");
        
        // Act: Try to merge at junction
        Either<String, Integer> result = workingTrain.map2(derailedTrain, Integer::sum);
        
        // Assert: Merge fails, error from second derailment propagates
        assertTrue(result.isLeft().toBoolean());
        assertEquals("brake failure", result.fold(e -> e, v -> null));
    }

    @Test
    void testThreeTrainConvergence_AllOnSuccessTrack() {
        // Arrange: Three trains converging at major railway junction
        Either<String, Integer> train1 = Either.right(1);
        Either<String, Integer> train2 = Either.right(2);
        Either<String, Integer> train3 = Either.right(3);
        
        // Act: Combine cargo from all three trains
        Either<String, Integer> result = train1.map3(train2, train3, 
            (cargo1, cargo2, cargo3) -> cargo1 + cargo2 + cargo3);
        
        // Assert: All cargo successfully combined
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(6), result.orNull());
    }

    @Test
    void testThreeTrainConvergence_MiddleTrainDerailed() {
        // Arrange: First and third trains fine, middle train derailed
        Either<String, Integer> train1 = Either.right(1);
        Either<String, Integer> derailedTrain = Either.left("signal malfunction");
        Either<String, Integer> train3 = Either.right(3);
        
        // Act: Try to combine at three-way junction
        Either<String, Integer> result = train1.map3(derailedTrain, train3, 
            (cargo1, cargo2, cargo3) -> cargo1 + cargo2 + cargo3);
        
        // Assert: Convergence fails due to middle train derailment
        assertTrue(result.isLeft().toBoolean());
        assertEquals("signal malfunction", result.fold(e -> e, v -> null));
    }

    // ====================== RAILWAY SWITCH TESTS (CONDITIONAL OPERATIONS) ======================

    @Test
    void testRailwaySwitch_TrainPassesInspection() {
        // Arrange: Train with valid cargo on success track
        Either<String, Integer> trainWithCargo = Either.right(10);
        
        // Act: Check cargo at railway inspection point
        Either<String, Integer> result = trainWithCargo.filter(
            cargo -> cargo > 5,  // Cargo inspection criteria
            () -> "Cargo insufficient - switching to error track"
        );
        
        // Assert: Train passes inspection, continues on success track
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(10), result.orNull());
    }

    @Test
    void testRailwaySwitch_TrainFailsInspection() {
        // Arrange: Train with insufficient cargo on success track
        Either<String, Integer> trainWithBadCargo = Either.right(3);
        
        // Act: Cargo fails inspection at switch point
        Either<String, Integer> result = trainWithBadCargo.filter(
            cargo -> cargo > 5,  // Cargo inspection criteria
            () -> "Cargo insufficient - switching to error track"
        );
        
        // Assert: Train gets switched to error track due to failed inspection
        assertTrue(result.isLeft().toBoolean());
        assertEquals("Cargo insufficient - switching to error track", result.fold(e -> e, v -> null));
    }

    @Test
    void testRailwaySwitch_TrainAlreadyOnErrorTrack() {
        // Arrange: Train already derailed on error track
        Either<String, Integer> derailedTrain = Either.left("previous derailment");
        
        // Act: Try to inspect already derailed train
        Either<String, Integer> result = derailedTrain.filter(
            cargo -> cargo > 5,
            () -> "New inspection failure"
        );
        
        // Assert: Train stays on error track, original error preserved
        assertTrue(result.isLeft().toBoolean());
        assertEquals("previous derailment", result.fold(e -> e, v -> null));
    }

    @Test
    void testRailwaySafetyCheck_TrainPassesInspection() {
        // Arrange: Train on success track approaching safety checkpoint
        Either<String, Integer> approachingTrain = Either.right(10);
        
        // Act: Safety system ensures cargo meets standards
        Either<String, Integer> result = approachingTrain.ensure(
            cargo -> cargo > 5,  // Safety standards
            () -> "Safety violation - emergency stop"
        );
        
        // Assert: Train passes safety check, continues journey
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(10), result.orNull());
    }

    // ====================== CARGO COUPLING TESTS (COMBINATION OPERATIONS) ======================

    @Test
    void testCargoCoupling_BothTrainsSuccessful() {
        // Arrange: Two trains with different cargo types on success track
        Either<String, Integer> cargoTrain1 = Either.right(3);
        Either<String, String> cargoTrain2 = Either.right("hello");
        
        // Act: Couple the cargo cars together at junction
        Either<String, Either.Tuple2<Integer, String>> result = cargoTrain1.zip(cargoTrain2);
        
        // Assert: Cargo successfully coupled into one shipment
        assertTrue(result.isRight().toBoolean());
        Either.Tuple2<Integer, String> coupledCargo = result.orNull();
        assertEquals(Integer.valueOf(3), coupledCargo.first());
        assertEquals("hello", coupledCargo.second());
    }

    @Test
    void testZipWithFirstLeft() {
        Either<String, Integer> either1 = Either.left("error1");
        Either<String, String> either2 = Either.right("hello");
        
        Either<String, Either.Tuple2<Integer, String>> result = either1.zip(either2);
        
        assertTrue(result.isLeft().toBoolean());
        assertEquals("error1", result.fold(e -> e, v -> null));
    }

    @Test
    void testZipWithFunction() {
        Either<String, Integer> either1 = Either.right(3);
        Either<String, Integer> either2 = Either.right(4);
        
        Either<String, String> result = either1.zipWith(either2, 
            (a, b) -> a + " + " + b + " = " + (a + b));
        
        assertTrue(result.isRight().toBoolean());
        assertEquals("3 + 4 = 7", result.orNull());
    }

    // ====================== EMERGENCY RECOVERY TESTS ======================

    @Test
    void testEmergencyRecovery_DerailedTrainRepaired() {
        // Arrange: Train derailed and on error track
        Either<String, Integer> derailedTrain = Either.left("engine malfunction");
        
        // Act: Emergency crew attempts to repair and get back on track
        Either<String, Integer> result = derailedTrain.recover(
            problemReport -> Either.right(42)  // Emergency repair successful
        );
        
        // Assert: Train successfully recovered and back on success track
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(42), result.orNull());
    }

    @Test
    void testEmergencyRecovery_TrainAlreadyRunning() {
        // Arrange: Train running fine on success track
        Either<String, Integer> runningTrain = Either.right(10);
        
        // Act: Emergency system checks if recovery needed
        Either<String, Integer> result = runningTrain.recover(
            problemReport -> Either.right(42)  // Recovery not needed
        );
        
        // Assert: Train continues normally, no recovery applied
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(10), result.orNull());
    }

    @Test
    void testEmergencyRecovery_RepairAttemptFails() {
        // Arrange: Train derailed with severe damage
        Either<String, Integer> severelyDamagedTrain = Either.left("total engine failure");
        
        // Act: Emergency crew attempts repair but fails
        Either<String, Integer> result = severelyDamagedTrain.recover(
            problemReport -> Either.left("repair impossible - total loss")
        );
        
        // Assert: Recovery failed, train remains on error track with new error
        assertTrue(result.isLeft().toBoolean());
        assertEquals("repair impossible - total loss", result.fold(e -> e, v -> null));
    }

    @Test
    void testMergeWithSameTypes() {
        Either<String, String> leftEither = Either.left("error value");
        Either<String, String> rightEither = Either.right("success value");
        
        String leftResult = leftEither.merge();
        String rightResult = rightEither.merge();
        
        assertEquals("error value", leftResult);
        assertEquals("success value", rightResult);
    }

    // ====================== UTILITY CONSTRUCTOR TESTS ======================

    @Test
    void testFromOptionalWithValue() {
        Optional<String> optional = Optional.of("value");
        
        Either<String, String> result = Either.fromOptional(
            () -> "No value",
            optional);
        
        assertTrue(result.isRight().toBoolean());
        assertEquals("value", result.orNull());
    }

    @Test
    void testFromOptionalEmpty() {
        Optional<String> optional = Optional.empty();
        
        Either<String, String> result = Either.fromOptional(
            () -> "No value",
            optional);
        
        assertTrue(result.isLeft().toBoolean());
        assertEquals("No value", result.fold(e -> e, v -> null));
    }

    @Test
    void testFromBooleanTrue() {
        Either<String, String> result = Either.fromBoolean(true, 
            () -> "failure",
            () -> "success");
        
        assertTrue(result.isRight().toBoolean());
        assertEquals("success", result.orNull());
    }

    @Test
    void testFromBooleanFalse() {
        Either<String, String> result = Either.fromBoolean(false, 
            () -> "failure",
            () -> "success");
        
        assertTrue(result.isLeft().toBoolean());
        assertEquals("failure", result.fold(e -> e, v -> null));
    }

    @Test
    void testFromTrySuccess() {
        Either<String, Integer> result = Either.fromTry(
            ex -> "Error: " + ex.getMessage(),
            () -> 42
        );
        
        assertTrue(result.isRight().toBoolean());
        assertEquals(Integer.valueOf(42), result.orNull());
    }

    @Test
    void testFromTryException() {
        Either<String, Integer> result = Either.fromTry(
            ex -> "Error: " + ex.getMessage(),
            () -> { throw new RuntimeException("Something went wrong"); }
        );
        
        assertTrue(result.isLeft().toBoolean());
        assertEquals("Error: Something went wrong", result.fold(e -> e, v -> null));
    }

    // ====================== LIFTING OPERATION TESTS ======================

    @Test
    void testLiftFunction() {
        Mapper<Integer, Integer> doubleMapper = x -> x * 2;
        var liftedFunction = Either.<String, Integer, Integer>lift(doubleMapper);
        
        Either<String, Integer> rightInput = Either.right(5);
        Either<String, Integer> leftInput = Either.left("error");
        
        Either<String, Integer> rightResult = liftedFunction.apply(rightInput);
        Either<String, Integer> leftResult = liftedFunction.apply(leftInput);
        
        assertTrue(rightResult.isRight().toBoolean());
        assertEquals(Integer.valueOf(10), rightResult.orNull());
        
        assertTrue(leftResult.isLeft().toBoolean());
        assertEquals("error", leftResult.fold(e -> e, v -> null));
    }

    @Test
    void testLift2Function() {
        var liftedAdd = Either.<String, Integer, Integer, Integer>lift2(Integer::sum);
        
        Either<String, Integer> result1 = liftedAdd.apply(Either.right(3), Either.right(4));
        Either<String, Integer> result2 = liftedAdd.apply(Either.left("error"), Either.right(4));
        
        assertTrue(result1.isRight().toBoolean());
        assertEquals(Integer.valueOf(7), result1.orNull());
        
        assertTrue(result2.isLeft().toBoolean());
        assertEquals("error", result2.fold(e -> e, v -> null));
    }

    // ====================== COLLECTION OPERATION TESTS ======================

    @Test
    void testTraverseAllSuccess() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
        
        Either<String, List<String>> result = Either.traverse(numbers, 
            (Integer x) -> Either.<String, String>right("Value: " + x));
        
        assertTrue(result.isRight().toBoolean());
        List<String> values = result.orNull();
        assertEquals(4, values.size());
        assertEquals("Value: 1", values.get(0));
        assertEquals("Value: 4", values.get(3));
    }

    @Test
    void testTraverseWithError() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
        
        Either<String, List<String>> result = Either.traverse(numbers, 
            (Integer x) -> x == 3 ? Either.<String, String>left("Error at 3") : Either.<String, String>right("Value: " + x));
        
        assertTrue(result.isLeft().toBoolean());
        assertEquals("Error at 3", result.fold(e -> e, v -> null));
    }

    @Test
    void testSequenceAllSuccess() {
        List<Either<String, Integer>> eithers = Arrays.asList(
            Either.right(1),
            Either.right(2),
            Either.right(3)
        );
        
        Either<String, List<Integer>> result = Either.sequence(eithers);
        
        assertTrue(result.isRight().toBoolean());
        List<Integer> values = result.orNull();
        assertEquals(3, values.size());
        assertEquals(Integer.valueOf(1), values.get(0));
        assertEquals(Integer.valueOf(3), values.get(2));
    }

    @Test
    void testSequenceWithError() {
        List<Either<String, Integer>> eithers = Arrays.asList(
            Either.right(1),
            Either.left("error"),
            Either.right(3)
        );
        
        Either<String, List<Integer>> result = Either.sequence(eithers);
        
        assertTrue(result.isLeft().toBoolean());
        assertEquals("error", result.fold(e -> e, v -> null));
    }

    @Test
    void testPartition() {
        List<Either<String, Integer>> eithers = Arrays.asList(
            Either.right(1),
            Either.left("error1"),
            Either.right(2),
            Either.left("error2"),
            Either.right(3)
        );
        
        Either.Tuple2<List<String>, List<Integer>> result = Either.partition(eithers);
        
        List<String> errors = result.first();
        List<Integer> successes = result.second();
        
        assertEquals(2, errors.size());
        assertEquals("error1", errors.get(0));
        assertEquals("error2", errors.get(1));
        
        assertEquals(3, successes.size());
        assertEquals(Integer.valueOf(1), successes.get(0));
        assertEquals(Integer.valueOf(2), successes.get(1));
        assertEquals(Integer.valueOf(3), successes.get(2));
    }

    // ====================== COMPLETE RAILWAY SYSTEM INTEGRATION TEST ======================

    @Test
    void testCompleteRailwaySystem_SuccessfulJourney() {
        /*
         * Railway Metaphor: Complete Train Journey Through Multiple Stations
         * 
         * This simulates a freight train carrying user data through a complete
         * railway network with multiple stations, inspections, and processing points.
         */
        
        // Station 1: Loading dock - load email cargo
        Either<String, String> emailTrain = Either.fromOptional(
            () -> "Loading failed: No email cargo available",
            Optional.of("user@example.com")
        );
        
        // Station 2: Loading dock - load name cargo  
        Either<String, String> nameTrain = Either.fromBoolean(
            true, // cargo available
            () -> "Loading failed: No name cargo available",
            () -> "John Doe"
        );
        
        // Station 3: Junction - merge cargo from both trains
        Either<String, String> mergedCargoTrain = emailTrain.map2(nameTrain, 
            (emailCargo, nameCargo) -> "Combined cargo: " + nameCargo + " <" + emailCargo + ">");
        
        // Station 4: Quality inspection checkpoint
        Either<String, String> inspectedTrain = mergedCargoTrain
            .filter(cargo -> cargo.contains("@"), () -> "Quality check failed: Invalid email format")
            .map(cargo -> "Quality approved: " + cargo);
        
        // Station 5: Final processing and storage facility
        Either<String, String> finalDestination = inspectedTrain.flatMap(cargo -> 
            Either.fromTry(
                ex -> "Storage facility error: " + ex.getMessage(),
                () -> "Successfully stored at destination: " + cargo
            )
        );
        
        // Assert: Train completed entire journey successfully
        assertTrue(finalDestination.isRight().toBoolean());
        assertEquals("Successfully stored at destination: Quality approved: Combined cargo: John Doe <user@example.com>", 
                     finalDestination.orNull());
    }

    @Test
    void testCompleteRailwaySystem_DerailmentAtLoadingDock() {
        /*
         * Railway Metaphor: Train Derails at First Loading Dock
         * 
         * This simulates what happens when the first train derails at the loading dock -
         * the entire railway operation is affected, and the error propagates through
         * the whole system without attempting unnecessary operations.
         */
        
        // Station 1: Loading dock failure - no email cargo available
        Either<String, String> derailedEmailTrain = Either.left("Loading dock disaster: Email cargo missing");
        
        // Station 2: Loading dock - name cargo ready but pointless now
        Either<String, String> nameTrain = Either.right("John Doe");
        
        // Station 3: Junction - can't merge because email train derailed
        Either<String, String> failedMerger = derailedEmailTrain.map2(nameTrain, 
            (emailCargo, nameCargo) -> "Combined cargo: " + nameCargo + " <" + emailCargo + ">");
        
        // Station 4: Quality inspection - bypassed due to derailment
        Either<String, String> bypassedInspection = failedMerger
            .filter(cargo -> cargo.contains("@"), () -> "Quality check failed")
            .map(cargo -> "Quality approved: " + cargo);
        
        // Station 5: Storage facility - never reached
        Either<String, String> unreachableDestination = bypassedInspection.flatMap(cargo -> 
            Either.right("Stored: " + cargo)
        );
        
        // Assert: Original derailment error propagated through entire system
        assertTrue(unreachableDestination.isLeft().toBoolean());
        assertEquals("Loading dock disaster: Email cargo missing", 
                     unreachableDestination.fold(errorReport -> errorReport, cargo -> null));
    }
}