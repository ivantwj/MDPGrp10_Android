package com.mdpgrp10.androidmobilecontrollermodule;

/**
 * Created by Jennifer on 2/19/2016.
 */
public class Utils {
    public static final String PREF_DB = "G10DB";
    public static final String SET_EXPLORE = "DB_EX";
    public static final String SET_SHORTEST_PATH = "DB_SH";
    //public static final String SET_STOP = "DB_SO";
    public static final String SET_UP = "DB_UP";
    public static final String SET_DOWN = "DB_DO";
    public static final String SET_LEFT = "DB_LE";
    public static final String SET_RIGHT = "DB_RI";
    public static final String SET_CMD1 = "DB_C1";
    public static final String SET_CMD2 = "DB_C2";
    /*public static final String SET_CMD3 = "DB_C3";
    public static final String SET_CMD4 = "DB_C4";
    public static final String SET_CMD5 = "DB_C5";

    /*public static final int Coord = 2131427439;
    public static final int coord_button = 2130903065;*/

    public static final String SET_EXPLORE_DEFAULT = "pexplore" + "\n";
    public static final String SET_SHORTEST_PATH_DEFAULT = "pshortestPath" + "\n";
    //public static final String SET_STOP_DEFAULT = "q,";
    public static final String SET_UP_DEFAULT = "hW1|";
    public static final String SET_DOWN_DEFAULT = "hS1|";
    public static final String SET_LEFT_DEFAULT = "hA1|";
    public static final String SET_RIGHT_DEFAULT = "hD1|";
    public static final String SET_CMD1_DEFAULT = "custom command 1";
    public static final String SET_CMD2_DEFAULT = "custom command 2";
    /*public static final String SET_CMD3_DEFAULT = "custom command 3";
    public static final String SET_CMD4_DEFAULT = "custom command 4";
    public static final String SET_CMD5_DEFAULT = "custom command 5";*/


    public static final String HEAD_POS_UP = "U";
    public static final String HEAD_POS_DOWN = "D";
    public static final String HEAD_POS_LEFT = "L";
    public static final String HEAD_POS_RIGHT = "R";

    public static int MAP_COLS = 20;
    public static int MAP_ROWS = 15;
    public static String defaultMap = "GRID 15 20 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";

    public static String[] processMapDescriptor(String mapInfo){
        String[] tmpRobot = new String[4];
        int tmpIndex;
        for(int i = 0; i < 7; i++) {
            tmpIndex = mapInfo.indexOf(" ");
            if(i > 2)
                tmpRobot[i-3] = mapInfo.substring(0, tmpIndex);
            mapInfo = mapInfo.substring(tmpIndex+1);
        }
        String[] updatedInfo = new String[4];
        System.arraycopy(tmpRobot, 0, updatedInfo, 0, 2);
        updatedInfo[2] = checkHead(tmpRobot[0], tmpRobot[1], tmpRobot[2], tmpRobot[3]);
        updatedInfo[3] = mapInfo;
        return updatedInfo;
    }

    public static String checkHead(String posX0, String posY0, String posX1, String posY1){
        if(posX0.equals(posX1)){
            if(Integer.valueOf(posY0) < Integer.valueOf(posY1))
                return  HEAD_POS_DOWN;
            else
                return HEAD_POS_UP;
        }else if(posY0.equals(posY1)){
            if(Integer.valueOf(posX0) < Integer.valueOf(posX1))
                return  HEAD_POS_RIGHT;
            else
                return HEAD_POS_LEFT;
        }else
            return "N";
    }
}

