package com;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;

import model.NewFeatures;
import model.Pos;
import model.Traj;

public class TravelMode_5 {
	public static final double DEGREES_TO_RADIANS=Math.PI/180.0;
	public static final double RADIANS_TO_DEGREES=180.0/Math.PI;
	public static final double EARTH_MEAN_DIAMETER=6371.009*2;
	public static int SplitTimeIntervalBefore=5;//min
	public static int SplitTimeInterval=30;//min
	public static int MinSpeedBefore=30;//  km/h
	public static int MinSpeed=5;//  km/h
	public static int MinDistance=2000;//m
	
	private static String EarlyStart="07:30:00";
	private static String EarlyEnd="09:00:00";
	private static String LateStart="17:30:00";
	private static String LateEnd="19:00:00";
	private static String NightStart="23:00:00";
	private static String NightEnd="06:00:00";
	
	public DataStream<String> Handle(DataStream<String> userTrack){
		return userTrack.filter(new FilterFunction<String>(){
			private static final long serialVersionUID = 1L;

			public boolean filter(String s) throws Exception {
				// TODO Auto-generated method stub
				String[] split_res=s.split(";");
				if(split_res.length<=2)	return false;
				return true;
			}
		}).flatMap(new FlatMapFunction<String,String>(){
			private static final long serialVersionUID = 1L;
			public void flatMap(String s, Collector<String> out) throws Exception {
				// TODO Auto-generated method stub
				String[] split_res=s.split(";");
				List<Pos> list_pos=new ArrayList<Pos>();
				
				int param=Integer.valueOf(split_res[1].split(",")[5]);
				//是否为车务通   0：不是   1：是
				int type=Integer.valueOf(split_res[1].split(",")[6]);
				for(int i=1;i<split_res.length;i++){
					String[] positions=split_res[i].split(",");
					Pos pos=new Pos();
					pos.date=positions[0];
					pos.lon=Double.valueOf(positions[1]);
					pos.lat=Double.valueOf(positions[2]);
					list_pos.add(pos);
				}
				Traj traj=new Traj();
				traj.Id=split_res[0];
				traj.lists=list_pos;
				traj.type=type;
				
				//分割
				List<List<Pos>> trips=GetSplitPosByTraj(traj);
				
				//凑成原数据格式
				for(int i=0;i<trips.size();i++){
					StringBuilder trajToString=new StringBuilder();
					trajToString.append(traj.Id+";");
					for(int j=0;j<trips.get(i).size();j++){
						Pos pos=trips.get(i).get(j);
						trajToString.append(pos.date+","+pos.lon+","+pos.lat+","+pos.lat+","+pos.lat+","+param+","+type);
						if(j!=trips.get(i).size()-1){
							trajToString.append(";");
						}
					}
					out.collect(trajToString.toString());
				}
			}
		}).map(new MapFunction<String,String>(){
			private static final long serialVersionUID = 1L;
			public String map(String s) throws Exception {
				// TODO Auto-generated method stub
				String[] split_res=s.split(";");
				if(split_res.length<=4)	return "-1";
				List<Pos> list_pos=new ArrayList<Pos>();
				//是否为车务通   0：不是   1：是
				int type=Integer.valueOf(split_res[1].split(",")[6]);
				for(int i=1;i<split_res.length;i++){
					String[] positions=split_res[i].split(",");
					Pos pos=new Pos();
					pos.date=positions[0];
					pos.lon=Double.valueOf(positions[1]);
					pos.lat=Double.valueOf(positions[2]);
					list_pos.add(pos);
				}
				Traj traj=new Traj();
				traj.Id=split_res[0];
				traj.lists=list_pos;
				traj.type=type;
				String time=split_res[1].split(",")[0];
				
				NewFeatures features=GetTravelFeatures(traj);
				int index=GetMaxMembershipDegreeIndex(features);
				return String.valueOf(index)+"-"+traj.type+"-"+traj.Id+"-"+time;
			}
		});
	}
}
