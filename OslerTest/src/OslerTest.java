import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OslerTest {
    public enum Source {API, Local};

    /*portal api and local device storage simulation. We return a list of UserRecord*/
    public static List<UserRecord> getFromSource(Source source) {
        String path = null;
        if (source == Source.API) path = "data/PortalUserList.txt";
        if (source == Source.Local) path = "data/DeviceUserList.txt";

        List<UserRecord> result = new ArrayList<UserRecord>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line, uuid, deviceId;
            int userStatus = 0;
            while ((line = reader.readLine()) != null) {
                String[] lineData = line.split("\t");
                uuid = lineData[0];
                deviceId = lineData[1];
                userStatus = Integer.decode(lineData[2]) & 0xFF; //no unsigned bytes in java. shame
                result.add(new UserRecord(uuid, deviceId, userStatus));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void updateUsers(List<UserRecord> localData, List<UserRecord> portalData, String saveTopath) throws IOException {
        //get the device Id from the localData. it's the same in all rows so we get it from the first record
        String deviceId = localData.get(0).getDeviceId();
        //filter out all UserRecords in portal data that match only this deviceID
        List<UserRecord> filteredRecords = portalData.stream().filter(userRecord -> userRecord.getDeviceId().equals(deviceId)).collect(Collectors.toList());
        List<UserRecord> updatedLocalData = new ArrayList<>();

        for (UserRecord userRecord : localData) {
            List<UserRecord> matchingRecords = filteredRecords.stream().filter(ur -> ur.getUuid().equals(userRecord.getUuid())).collect(Collectors.toList());
            if (matchingRecords.size() > 0) //we found some portal updates. NB: they could be more than 1
            {
                userRecord.resetUserStatus(); //reset to prepare for update
                for (UserRecord musr : matchingRecords)
                    userRecord.updateUserStatus(musr.getUserStatus());
            }
            updatedLocalData.add(userRecord);
        }
        updatedLocalData = new ArrayList<UserRecord>(Set.copyOf(updatedLocalData)); //remove duplicates

        BufferedWriter writer = new BufferedWriter(new FileWriter(saveTopath));
        for (UserRecord record : updatedLocalData) {
            //String line = String.join("\t",record.getUuid(),record.getDeviceId(),record.getAuthStatus(),record.getTrainingStatus());
            String line = record.getUuid() + "\t" + record.getDeviceId() + "\t" + record.getAuthStatus() + "\t" + record.getTrainingStatus() + "\n";
            writer.write(line);
        }
        writer.close();
    }

    public static void main(String[] args) {
        List<UserRecord> portalData = getFromSource(Source.API); //call the api
        List<UserRecord> localData = getFromSource(Source.Local); //get local data
        try {
            updateUsers(localData, portalData, "data/DeviceUserListUpdated.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
