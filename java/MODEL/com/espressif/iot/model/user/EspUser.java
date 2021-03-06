package com.espressif.iot.model.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import android.net.wifi.ScanResult;

import com.espressif.iot.action.device.New.EspActionDeviceNewGetInfoLocal;
import com.espressif.iot.action.device.New.IEspActionDeviceNewGetInfoLocal;
import com.espressif.iot.action.device.common.EspActionDeviceActivateSharedInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGenerateShareKeyInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGetStatusInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGetStatusLocal;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.EspActionDeviceSleepRebootLocal;
import com.espressif.iot.action.device.common.EspActionDeviceSynchronizeInterentDiscoverLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceActivateSharedInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGenerateShareKeyInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGetStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGetStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceSleepRebootLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceSynchronizeInterentDiscoverLocal;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerDeleteInternet;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerGetInternet;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerPostInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerDeleteInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerGetInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerPostInternet;
import com.espressif.iot.action.device.humiture.EspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.device.humiture.IEspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.user.EspActionUserDevicesUpdated;
import com.espressif.iot.action.user.EspActionUserLoginDB;
import com.espressif.iot.action.user.EspActionUserLoginInternet;
import com.espressif.iot.action.user.EspActionUserRegisterInternet;
import com.espressif.iot.action.user.IEspActionUserDevicesUpdated;
import com.espressif.iot.action.user.IEspActionUserLoginDB;
import com.espressif.iot.action.user.IEspActionUserLoginInternet;
import com.espressif.iot.action.user.IEspActionUserRegisterInternet;
import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.db.IOTUserDBManager;
import com.espressif.iot.db.greenrobot.daos.ApDB;
import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.device.builder.BEspDeviceNew;
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine.Direction;
import com.espressif.iot.device.upgrade.IEspDeviceCheckCompatibility;
import com.espressif.iot.device.upgrade.IEspDeviceGetUpgradeTypeResult;
import com.espressif.iot.model.adt.tree.EspDeviceTreeElement;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachine;
import com.espressif.iot.model.device.upgrade.EspDeviceCheckCompatibility;
import com.espressif.iot.model.device.upgrade.EspDeviceGetUpgradeTypeResult;
import com.espressif.iot.object.db.IApDB;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.RandomUtil;
import com.espressif.iot.util.RouterUtil;
import com.espressif.iot.util.TimeUtil;

public class EspUser implements IEspUser
{
    
    private final static Logger log = Logger.getLogger(EspUser.class);
    
    private long mUserId;
    
    private String mUserKey;
    
    private String mUserEmail;
    
    private String mUserPassword;
    
    private boolean mIsPwdSaved;
    
    private boolean mAutoLogin;
    
    private List<IEspDevice> mDeviceList = new ArrayList<IEspDevice>();
    
    @Override
    public String toString()
    {
        return "[id=" + mUserId + ",key=" + mUserKey + ",email=" + mUserEmail + ",isPwdSaved=" + mIsPwdSaved
            + ",mAutoLogin=" + mAutoLogin + ",password=" + mUserPassword + "]";
    }
    
    @Override
    public Void saveUserInfoInDB(final boolean isPwdSaved, final boolean isAutoLogin)
    {
        this.mIsPwdSaved = isPwdSaved;
        this.mAutoLogin = isAutoLogin;
        IOTUserDBManager.getInstance().changeUserInfo(mUserId,
            mUserEmail,
            mUserPassword,
            mUserKey,
            isPwdSaved,
            isAutoLogin);
        return null;
    }
    
    @Override
    public void setUserEmail(String userEmail)
    {
        this.mUserEmail = userEmail;
    }
    
    @Override
    public String getUserEmail()
    {
        return this.mUserEmail;
    }
    
    @Override
    public void setUserId(long userId)
    {
        this.mUserId = userId;
    }
    
    @Override
    public long getUserId()
    {
        return this.mUserId;
    }
    
    @Override
    public void setUserKey(String userKey)
    {
        this.mUserKey = userKey;
    }
    
    @Override
    public String getUserKey()
    {
        return this.mUserKey;
    }
    
    @Override
    public void setUserPassword(String userPassword)
    {
        this.mUserPassword = userPassword;
    }
    
    @Override
    public String getUserPassword()
    {
        return this.mUserPassword;
    }
    
    @Override
    public void setIsPwdSaved(boolean isPwdSaved)
    {
        this.mIsPwdSaved = isPwdSaved;
    }
    
    @Override
    public boolean isPwdSaved()
    {
        return this.mIsPwdSaved;
    }
    
    @Override
    public void setAutoLogin(boolean autoLogin)
    {
        this.mAutoLogin = autoLogin;
    }
    
    @Override
    public boolean isAutoLogin()
    {
        return this.mAutoLogin;
    }
    
    @Override
    public List<IEspDevice> getDeviceList()
    {
        return mDeviceList;
    }
    
    private String mLastConnectedSsid;
    
    @Override
    public void setLastConnectedSsid(String ssid)
    {
        mLastConnectedSsid = ssid;
    }
    
    @Override
    public String getLastConnectedSsid()
    {
        return mLastConnectedSsid;
    }
    
    @Override
    public String getLastSelectedApBssid()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        IApDB apDB = iotApDBManager.getLastSelectedApDB();
        if (apDB == null)
        {
            return null;
        }
        else
        {
            return apDB.getBssid();
        }
    }
    
    @Override
    public String getLastSelectedApPassword()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        IApDB apDB = iotApDBManager.getLastSelectedApDB();
        if (apDB == null)
        {
            return null;
        }
        else
        {
            return apDB.getPassword();
        }
    }
    
    @Override
    public List<String[]> getConfiguredAps()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        List<ApDB> apDBs = iotApDBManager.getAllApDBList();
        List<String[]> result = new ArrayList<String[]>();
        for (ApDB apDB : apDBs)
        {
            String[] ap = new String[3];
            ap[0] = apDB.getBssid();
            ap[1] = apDB.getSsid();
            ap[2] = apDB.getPassword();
            result.add(ap);
        }
        return result;
    }
    
    @Override
    public String getApPassword(String bssid)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        return iotApDBManager.getPassword(bssid);
    }
    
    @Override
    public void saveApInfoInDB(String bssid, String ssid, String password)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        iotApDBManager.insertOrReplace(bssid, ssid, password);
    } 
    
    @Override
    public void saveApInfoInDB(String bssid, String ssid, String password, String deviceBssid)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        iotApDBManager.insertOrReplace(bssid, ssid, password, deviceBssid);
    }
    
    @Override
    public IEspDevice getUserDevice(String deviceKey)
    {
        // Check Root router device
        IEspDevice deviceRoot = BEspDeviceRoot.getBuilder().getLocalRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        deviceRoot = BEspDeviceRoot.getBuilder().getInternetRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        
        for (IEspDevice device : mDeviceList)
        {
            // if the device is DELETED, ignore it
            if (device.getDeviceState().isStateDeleted())
            {
                continue;
            }
            if (deviceKey.equals(device.getKey()))
            {
                return device;
            }
        }
        return null;
    }
    
    @Override
    public void doActionConfigure(IEspDevice device, String apSsid, WifiCipherType apWifiCipherType, String apPassword)
    {
        String randomToken = RandomUtil.random40();
        IEspDeviceNew deviceNew = (IEspDeviceNew)device;
        deviceNew.setApPassword(apPassword);
        deviceNew.setApSsid(apSsid);
        deviceNew.setApWifiCipherType(apWifiCipherType);
        deviceNew.setKey(randomToken);
        deviceNew.setUserId(mUserId);
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.CONFIGURE);
    }
    
    @Override
    public boolean doActionPostDeviceStatus(IEspDevice device, IEspDeviceStatus status)
    {
        return doActionPostDeviceStatus(device, status, false);
    }
    
    @Override
    public boolean doActionPostDeviceStatus(IEspDevice device, IEspDeviceStatus status, boolean isBroadcast)
    {
        boolean isLocal = device.getDeviceState().isStateLocal();
        if (isLocal)
        {
            IEspActionDevicePostStatusLocal actionLocal = new EspActionDevicePostStatusLocal();
            return actionLocal.doActionDevicePostStatusLocal(device, status, isBroadcast);
        }
        else
        {
            IEspActionDevicePostStatusInternet actionInternet = new EspActionDevicePostStatusInternet();
            return actionInternet.doActionDevicePostStatusInternet(device, status, isBroadcast);
        }
    }
    
    @Override
    public boolean doActionGetDeviceStatus(IEspDevice device)
    {
        boolean isLocal = device.getDeviceState().isStateLocal();
        if (isLocal)
        {
            IEspActionDeviceGetStatusLocal actionLocal = new EspActionDeviceGetStatusLocal();
            return actionLocal.doActionDeviceGetStatusLocal(device);
        }
        else
        {
            IEspActionDeviceGetStatusInternet actionInternet = new EspActionDeviceGetStatusInternet();
            return actionInternet.doActionDeviceGetStatusInternet(device);
        }
    }
    
    @Override
    public void doActionDelete(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.DELETE);
    }
    
    @Override
    public void doActionDelete(final Collection<IEspDevice> devices)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(devices, Direction.DELETE);
    }
    
    @Override
    public void doActionRename(IEspDevice device, String deviceName)
    {
        device.setName(deviceName);
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.RENAME);
    }
    
    @Override
    public void doActionUpgradeLocal(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.UPGRADE_LOCAL);
    }
    
    @Override
    public void doActionUpgradeInternet(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.UPGRADE_INTERNET);
    }
    
    @Override
    public void doActionRefreshDevices()
    {
        IEspActionDeviceSynchronizeInterentDiscoverLocal action = new EspActionDeviceSynchronizeInterentDiscoverLocal();
        action.doActionDeviceSynchronizeInterentDiscoverLocal(mUserKey);
    }
    
    private void __loadUserDeviceList()
    {
        IOTUserDBManager iotUserDBManager = IOTUserDBManager.getInstance();
        List<DeviceDB> deviceDBList = iotUserDBManager.getUserDeviceList(mUserId);
        mDeviceList = new ArrayList<IEspDevice>();
        // add device into mDeviceList by deviceDBList
        for (DeviceDB deviceDB : deviceDBList)
        {
            IEspDevice device = BEspDevice.getInstance().alloc(deviceDB);
            IEspDeviceState deviceState = device.getDeviceState();
            // if the device state is ACTIVATING, resume activating
            if (EspDeviceState.checkValidWithSpecificStates(deviceState, EspDeviceState.ACTIVATING))
            {
                IEspDeviceNew deviceNew = (IEspDeviceNew)device;
                deviceNew.resume();
            }
            // else LOCAL, INTERNET, UPGRADEING_LOCAL, UPGRADING_INTERNET should be set OFFLINE
            else
            {
                if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.LOCAL,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateLocal();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState,
                    EspDeviceState.LOCAL,
                    EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.OFFLINE)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.OFFLINE,
                        EspDeviceState.RENAMED))
                {
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.UPGRADING_LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.UPGRADING_LOCAL,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateUpgradingLocal();
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.UPGRADING_INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.UPGRADING_INTERNET,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateUpgradingInternet();
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithSpecificStates(deviceState, EspDeviceState.DELETED))
                {
                }
                
                else
                {
                    throw new IllegalStateException("device: " + device);
                }
            }
            mDeviceList.add(device);
        }
        // sort device list
        EspDeviceGenericComparator comparator = new EspDeviceGenericComparator();
        Collections.sort(mDeviceList, comparator);
        
        // do rename action or delete action if necessay
        for (IEspDevice device : mDeviceList)
        {
            IEspDeviceState deviceState = device.getDeviceState();
            if (deviceState.isStateRenamed() && !deviceState.isStateDeleted())
            {
                doActionRename(device, device.getName());
            }
            else if (deviceState.isStateDeleted())
            {
                doActionDelete(device);
            }
        }
    }
    
    @Override
    public EspLoginResult doActionUserLoginInternet(String userEmail, String userPassword, boolean isPwdSaved,
        boolean isAutoLogin)
    {
        IEspActionUserLoginInternet action = new EspActionUserLoginInternet();
        EspLoginResult result = action.doActionUserLoginInternet(userEmail, userPassword, isPwdSaved, isAutoLogin);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public IEspUser doActionUserLoginDB()
    {
        IEspActionUserLoginDB action = new EspActionUserLoginDB();
        IEspUser result = action.doActionUserLoginDB();
        if (result.isAutoLogin())
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public EspRegisterResult doActionUserRegisterInternet(String userName, String userEmail, String userPassword)
    {
        IEspActionUserRegisterInternet action = new EspActionUserRegisterInternet();
        return action.doActionUserRegisterInternet(userName, userEmail, userPassword);
    }
    
    @Override
    public Void doActionDevicesUpdated(boolean isStateMachine)
    {
        IEspActionUserDevicesUpdated action = new EspActionUserDevicesUpdated();
        return action.doActionDevicesUpdated(isStateMachine);
    }
    
    // "ESP_" + MAC address's 6 places
    private boolean isESPDevice(String SSID)
    {
        for (int i = 0; i < DEVICE_SSID_PREFIX.length; i++)
        {
            if (SSID.startsWith(DEVICE_SSID_PREFIX[i]))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<IEspDeviceNew> scanSoftapDeviceList()
    {
        return scanSoftapDeviceList(true);
    }
    
    @Override
    public List<IEspDeviceNew> scanSoftapDeviceList(boolean isFilter)
    {
        List<IEspDeviceNew> softapDeviceList = new ArrayList<IEspDeviceNew>();
        List<ScanResult> scanResultList = EspBaseApiUtil.scan();
        for (ScanResult scanResult : scanResultList)
        {
            if (isESPDevice(scanResult.SSID))
            {
                String ssid = scanResult.SSID;
                // change the device bssid to sta
                String bssid = BSSIDUtil.restoreStaBSSID(scanResult.BSSID);
                int rssi = scanResult.level;
                WifiCipherType wifiCipherType = WifiCipherType.getWifiCipherType(scanResult);
                int state = EspDeviceState.NEW.getStateValue();
                IEspDeviceNew deviceNew = BEspDeviceNew.getInstance().alloc(ssid, bssid, wifiCipherType, rssi, state);
                deviceNew.getDeviceState().addStateNew();
                softapDeviceList.add(deviceNew);
            }
        }
        
        // filter device configured just now
        // List<IEspDevice> mDeviceList
        if (isFilter)
        {
            String bssidSoftap;
            String bssidDevice;
            long timestampConfigure;
            for (IEspDevice device : mDeviceList)
            {
                for (int i = 0; i < softapDeviceList.size(); i++)
                {
                    bssidSoftap = softapDeviceList.get(i).getBssid();
                    bssidDevice = device.getBssid();
                    if (bssidSoftap.equals(bssidDevice))
                    {
                        timestampConfigure = device.getTimestamp();
                        log.error("timestampConfigure = " + TimeUtil.getDateStr(timestampConfigure, null));
                        if (System.currentTimeMillis() - timestampConfigure < SOFTAP_IGNORE_TIMESTAMP
                            || EspDeviceState.checkValidWithSpecificStates(device.getDeviceState(),
                                EspDeviceState.ACTIVATING))
                        {
                            log.error("device = " + device + " is removed");
                            softapDeviceList.remove(i);
                        }
                        break;
                    }
                }
            }
        }
        
        return softapDeviceList;
    }
    
    @Override
    public String doActionGenerateShareKey(String ownerDeviceKey)
    {
        IEspActionDeviceGenerateShareKeyInternet action = new EspActionDeviceGenerateShareKeyInternet();
        return action.doActionDeviceGenerateShareKeyInternet(ownerDeviceKey);
    }
    
    @Override
    public boolean doActionActivateSharedDevice(String sharedDeviceKey)
    {
        IEspActionDeviceActivateSharedInternet action = new EspActionDeviceActivateSharedInternet();
        return action.doActionDeviceActivateSharedInternet(mUserId, mUserKey, sharedDeviceKey);
    }
    
    @Override
    public EspUpgradeDeviceCompatibility checkDeviceCompatibility(IEspDevice device)
    {
        String version = device.getRom_version();
        IEspDeviceCheckCompatibility action = new EspDeviceCheckCompatibility();
        return action.checkDeviceCompatibility(version);
    }
    
    @Override
    public EspUpgradeDeviceTypeResult getDeviceUpgradeTypeResult(IEspDevice device)
    {
        String romVersion = device.getRom_version();
        String latestRomVersion = device.getLatest_rom_version();
        IEspDeviceGetUpgradeTypeResult action = new EspDeviceGetUpgradeTypeResult();
        return action.getDeviceUpgradeTypeResult(romVersion, latestRomVersion);
    }
    
    @Override
    public boolean doActionDeviceTimerGet(IEspDevice device)
    {
        IEspActionDeviceTimerGetInternet action = new EspActionDeviceTimerGetInternet();
        return action.doActionDeviceTimerGet(device);
    }
    
    @Override
    public boolean doActionDeviceTimerPost(IEspDevice device, JSONObject timerJSON)
    {
        IEspActionDeviceTimerPostInternet action = new EspActionDeviceTimerPostInternet();
        return action.doActionDeviceTimerPostInternet(device, timerJSON);
    }
    
    @Override
    public boolean doActionDeviceTimerDelete(IEspDevice device, long timerId)
    {
        IEspActionDeviceTimerDeleteInternet action = new EspActionDeviceTimerDeleteInternet();
        return action.doActionDeviceTimerDeleteInternet(device, timerId);
    }
    
    @Override
    public DeviceInfo doActionDeviceNewConnect(IEspDeviceNew device)
    {
        IEspActionDeviceNewGetInfoLocal action = new EspActionDeviceNewGetInfoLocal();
        return action.doActionNewGetInfoLocal(device);
    }
    
    @Override
    public void doActionDeviceSleepRebootLocal(EspDeviceType type)
    {
        IEspActionDeviceSleepRebootLocal action = new EspActionDeviceSleepRebootLocal();
        action.doActionDeviceSleepRebootLocal(type);
    }
    
    @Override
    public List<IEspStatusHumiture> doActionGetHumitureStatusList(IEspDevice device, long startTimestamp,
        long endTimestamp, long interval)
    {
        IEspActionHumitureGetStatusListInternetDB action = new EspActionHumitureGetStatusListInternetDB();
        long deviceId = device.getId();
        String deviceKey = device.getKey();
        return action.doActionHumitureGetStatusListInternetDB(deviceId,
            deviceKey,
            startTimestamp,
            endTimestamp,
            interval);
    }
    
    @Override
    public List<IEspStatusFlammable> doActionGetFlammableStatusList(IEspDevice device, long startTimestamp,
        long endTimestamp, long interval)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    class EspDeviceGenericComparator implements Comparator<IEspDevice>
    {
        
        @Override
        public int compare(IEspDevice lhs, IEspDevice rhs)
        {
            String device1Name = lhs.getName();
            String device2Name = rhs.getName();
            /**
             * the order is determined by deviceName firstly
             */
            int result = device1Name.compareTo(device2Name);
            if (result == 0)
            {
                String bssid1 = lhs.getBssid();
                String bssid2 = rhs.getBssid();
                /**
                 * if deviceName is the same, it will determined by the bssid
                 */
                return bssid1.compareTo(bssid2);
            }
            return result;
        }
    }

    @Override
    public List<IEspDeviceTreeElement> getAllDeviceTreeElementList()
    {
        String router = null;
        String routerTemp = null;
        String parentRouter = null;
        String parentDeviceKey = null;
        boolean hasParent = false;
        boolean hasChild = false;
        int level = -1;
        // get routerList
        List<String> routerList = new ArrayList<String>();
        for (IEspDevice deviceInList : mDeviceList)
        {
            router = deviceInList.getRouter();
            if (router != null)
            {
                routerList.add(router);
            }
        }
        // build deviceTreeElementList
        List<IEspDeviceTreeElement> deviceTreeElementList = new ArrayList<IEspDeviceTreeElement>();
        for (IEspDevice deviceInList : mDeviceList)
        {
            router = deviceInList.getRouter();
            // don't forget to clear dirty info
            parentRouter = null;
            parentDeviceKey = null;
            hasParent = false;
            hasChild = false;
            level = -1;
            if (router != null)
            {
                level = RouterUtil.getRouterLevel(router);
                parentRouter = RouterUtil.getParentRouter(routerList, router);
                for (IEspDevice deviceInList2 : mDeviceList)
                {
                    routerTemp = deviceInList2.getRouter();
                    if (routerTemp != null && routerTemp.equals(parentRouter))
                    {
                        parentDeviceKey = deviceInList2.getKey();
                        break;
                    }
                }
                hasParent = parentRouter != null;
                hasChild = !RouterUtil.getDirectChildRouterList(routerList, router).isEmpty();
                IEspDeviceTreeElement deviceTreeElement =
                    new EspDeviceTreeElement(deviceInList, parentDeviceKey, hasParent, hasChild, level);
                deviceTreeElementList.add(deviceTreeElement);
            }
        }
        return deviceTreeElementList;
    }

    
}
