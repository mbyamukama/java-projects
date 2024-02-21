import java.util.Objects;

public class UserRecord {
    private String uuid;
    private String deviceId;
    private int userStatus;
    private enum AuthStatus {AuthorisedAdmin, AuthorisedOperator, DisabledAdmin, DisabledOperator};
    private enum TrainingStatus {Trained, Untrained};
    public UserRecord(String uuid, String deviceId, int userStatus) {
        this.uuid = uuid;
        this.deviceId = deviceId;
        this.userStatus = userStatus;
    }
    public String getUuid() {
        return uuid;
    }

    /*a. Bit 7 – Authorisation Status (1 = Authorised to access device, 0 = Disabled)
    b. Bit 6 – User’s Training Status (1 = Trained on device, 0 = Training is out of date)
    c. Bit 5 – Admin Status (1 = User is an Operator, 0 = User is an Administrator)
      0b10100000    0xA0    Authorised Operator
      0b00100000	0x20    Disabled Operator
      0b10000000    0x80    Authorised Administrator
      0b00000000    0x00    Disabled Administrator*/
    public AuthStatus getAuthStatus() {
        AuthStatus authStatus = null;
        if ((userStatus & 0xA0) == 0xA0)
            authStatus = AuthStatus.AuthorisedOperator;
        else if ((userStatus & 0x20) == 0x20)
            authStatus = AuthStatus.DisabledOperator;
        else if ((userStatus & 0x80) == 0x80)
            authStatus = AuthStatus.AuthorisedAdmin;
        else if (userStatus == 0x00 || userStatus == 0x40) //check bits 7,5 = 0. training Bit 6 may be set or not
            authStatus = AuthStatus.DisabledAdmin;
        return authStatus;
    }
    /* 0b01000000 0x40 Trained
       0b00000000 0x00 UnTrained
    * */
    public TrainingStatus getTrainingStatus()
    {
        TrainingStatus tStatus=null;
        if((userStatus & 0x40)==0x40)
            tStatus= TrainingStatus.Trained;
        if((userStatus & 0x40)==0x00) //check bit 6 = 0
            tStatus= TrainingStatus.Untrained;
        return  tStatus;
    }
    public int getUserStatus() {
        return userStatus;
    }

    public void resetUserStatus() {
        userStatus = 0;
    }

    public void updateUserStatus(int value) {
        userStatus = userStatus | value;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRecord temp = (UserRecord) o;
        return uuid.equals(temp.uuid) && userStatus == temp.userStatus && (deviceId.equals(temp.deviceId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, deviceId, userStatus);
    }
}
