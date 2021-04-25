package se.vendler;

public class FloorheatUpdateConsumer implements EventConsumer {
    private FloorheatService floorheatService;

    public FloorheatUpdateConsumer(FloorheatService floorheatService) {
        this.floorheatService = floorheatService;
    }


    @Override
    public void consume(Event event) {

    }

    @Override
    public void consume(FloorheatUpdate floorheatUpdate) {
        if(floorheatUpdate.isOn()){
            floorheatService.updateRoomStateOn(floorheatUpdate.getRoomId());
        }else{
            floorheatService.updateRoomStateOff(floorheatUpdate.getRoomId());
        }
    }
}
