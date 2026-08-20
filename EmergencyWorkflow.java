import java.util.ArrayDeque;
import java.util.Deque;

public class EmergencyWorkflow {

    // -----------------------------
    // State ของระบบ
    // -----------------------------
    enum State {
        NEW,
        RECEIVED,
        ASSIGNED,
        DISPATCHED,
        ON_SCENE,
        CLOSED
    }

    // -----------------------------
    // Action ของระบบ
    // -----------------------------
    enum Action {
        CALL_RECEIVED,
        TEAM_ASSIGNED,
        VEHICLE_DISPATCHED,
        ARRIVED_AT_SCENE,
        CASE_CLOSED
    }

    // -----------------------------
    // เก็บข้อมูลของ Action
    // -----------------------------
    static class Event {

        Action action;
        State fromState;
        State toState;

        Event(Action action, State fromState, State toState) {
            this.action = action;
            this.fromState = fromState;
            this.toState = toState;
        }

        @Override
        public String toString() {
            return action + " : " + fromState + " -> " + toState;
        }
    }

    // State ปัจจุบัน
    static State currentState = State.NEW;

    // Stack สำหรับเก็บ Event ที่ทำสำเร็จ
    static Deque<Event> eventStack = new ArrayDeque<>();

    // Stack สำหรับเก็บ Event ที่ Undo
    static Deque<Event> redoStack = new ArrayDeque<>();


    // =====================================================
    // Algorithm B : ตรวจสอบ Transition ด้วย State Machine
    // =====================================================

    static State getNextState(State state, Action action) {

        switch (state) {

            case NEW:
                if (action == Action.CALL_RECEIVED) {
                    return State.RECEIVED;
                }
                break;

            case RECEIVED:
                if (action == Action.TEAM_ASSIGNED) {
                    return State.ASSIGNED;
                }
                break;

            case ASSIGNED:
                if (action == Action.VEHICLE_DISPATCHED) {
                    return State.DISPATCHED;
                }
                break;

            case DISPATCHED:
                if (action == Action.ARRIVED_AT_SCENE) {
                    return State.ON_SCENE;
                }
                break;

            case ON_SCENE:
                if (action == Action.CASE_CLOSED) {
                    return State.CLOSED;
                }
                break;

            case CLOSED:
                break;
        }

        return null;
    }


    // =====================================================
    // เพิ่ม Action ใหม่
    // =====================================================

    static void addAction(Action action) {

        State nextState = getNextState(currentState, action);

        // ตรวจสอบ Action ผิดลำดับ
        if (nextState == null) {

            System.out.println(
                " Action " + action +
                " ผิดลำดับ เพราะ Current State = "
                + currentState
            );

            return;
        }

        // สร้าง Event
        Event event =
            new Event(action, currentState, nextState);

        // เพิ่มเข้า Event Stack
        eventStack.push(event);

        // เปลี่ยน State
        currentState = nextState;

        // เมื่อเพิ่ม Action ใหม่ ต้องล้าง Redo Stack
        redoStack.clear();

        System.out.println(
            " เพิ่ม " + action +
            " สำเร็จ : State = "
            + currentState
        );
    }


    // =====================================================
    // Undo
    // =====================================================

    static void undo() {

        // ไม่มี Action ให้ Undo
        if (eventStack.isEmpty()) {

            System.out.println(" ไม่สามารถ Undo ได้ เพราะ Event Stack ว่าง");

            return;
        }

        // เอา Action ล่าสุดออก
        Event event = eventStack.pop();

        // เก็บไว้ใน Redo Stack
        redoStack.push(event);

        // ย้อนกลับไป State ก่อนหน้า
        currentState = event.fromState;

        System.out.println(
            " Undo : " + event.action +
            " -> State = " + currentState
        );
    }


    // =====================================================
    // Redo
    // =====================================================

    static void redo() {

        // ไม่มี Action ให้ Redo
        if (redoStack.isEmpty()) {

            System.out.println(
                " ไม่สามารถ Redo ได้ เพราะ Redo Stack ว่าง"
            );

            return;
        }

        // ดู Action ที่จะ Redo
        Event event = redoStack.peek();

        // ตรวจสอบว่า State ปัจจุบันตรงกับ State ก่อน Action หรือไม่
        if (event.fromState != currentState) {

            System.out.println(
                " ไม่สามารถ Redo ได้ เพราะ State ไม่ตรงกัน"
            );

            return;
        }

        // เอาออกจาก Redo Stack
        redoStack.pop();

        // กลับเข้า Event Stack
        eventStack.push(event);

        // เปลี่ยน State
        currentState = event.toState;

        System.out.println(
            " Redo : " + event.action +
            " -> State = " + currentState
        );
    }


    // =====================================================
    // แสดงข้อมูล Stack
    // =====================================================

    static void showStatus() {

        System.out.println("\n==============================");
        System.out.println("Current State : " + currentState);

        System.out.println("\nEvent Stack:");

        if (eventStack.isEmpty()) {
            System.out.println("ว่าง");
        } else {

            for (Event event : eventStack) {
                System.out.println(event);
            }
        }

        System.out.println("\nRedo Stack:");

        if (redoStack.isEmpty()) {
            System.out.println("Free");
        } else {

            for (Event event : redoStack) {
                System.out.println(event);
            }
        }

        System.out.println("==============================\n");
    }


    // =====================================================
    // Main
    // =====================================================

    public static void main(String[] args) {

        System.out.println("===== Emergency Workflow =====");

        // Workflow ถูกต้อง
        addAction(Action.CALL_RECEIVED);
        addAction(Action.TEAM_ASSIGNED);
        addAction(Action.VEHICLE_DISPATCHED);
        addAction(Action.ARRIVED_AT_SCENE);
        addAction(Action.CASE_CLOSED);

        showStatus();

        // Undo 2 ครั้ง
        undo();
        undo();

        showStatus();

        // Redo 1 ครั้ง
        redo();

        showStatus();

        // เพิ่ม Action ใหม่
        // จะทำให้ Redo Stack ถูกล้าง
        addAction(Action.ARRIVED_AT_SCENE);

        showStatus();
    }
}