package com;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;

public class OutlierDiscovery_3 {
	public DataStream<String> Handle(DataStream<String> userTrack) {
		return userTrack.map(new MapFunction<String,String>(){
			public String map(String s) throws Exception {
				// TODO Auto-generated method stub
                String[] splitData = s.split(";");
                String userID = splitData[0];
                StringBuffer res = new StringBuffer(userID);
                res.append(";");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // 窗口大小
                int w = 5;
                double max_speed = 38.8;  // 最大速度，单位米每秒 (140km/h)
                double min_angle = 45; // 最小角度
                double mid_angle = 90;  // 适中的角度
                // 取第一第二个点的信息

                Double[] distance = new Double[w - 1];
                Double[] time = new Double[w - 1];

                // 计算第一点和第二个点各自与其下一个点的距离、时间、角度等

                String[] first_point = splitData[1].split(",");
                String[] second_point = splitData[2].split(",");
                String[] third_point = splitData[3].split(",");
                String[] fouth_point = splitData[4].split(",");
                time[0] = (df.parse(second_point[0]).getTime() - df.parse(first_point[0]).getTime()) / 1000.0 + 0.1;
                distance[0] = cal_dist(Double.parseDouble(first_point[1]),
                        Double.parseDouble(first_point[2]),
                        Double.parseDouble(second_point[1]),
                        Double.parseDouble(second_point[2]));

                time[1] = (df.parse(third_point[0]).getTime() - df.parse(second_point[0]).getTime()) / 1000.0 + 0.1;
                distance[1] = cal_dist(Double.parseDouble(second_point[1]),
                        Double.parseDouble(second_point[2]),
                        Double.parseDouble(third_point[1]),
                        Double.parseDouble(third_point[2]));
                // 计算当前点的角度，即点234的夹角
                double cur_angle = getDegree(
                        Double.parseDouble(third_point[1]),
                        Double.parseDouble(third_point[2]),
                        Double.parseDouble(second_point[1]),
                        Double.parseDouble(second_point[2]),
                        Double.parseDouble(fouth_point[1]),
                        Double.parseDouble(fouth_point[2])
                );

                String[] cur_point = third_point;
                String[] next_point1 = fouth_point;
                List<Integer> normal_index = new ArrayList<Integer>();
                // 从第一组的中间点开始循环
                int len = splitData.length - 1;

                for (int i = w / 2 + 1; i < len - w / 2; ++i) {
                    //  获取最新点的信息
                    String[] next_point2 = splitData[i + 2].split(",");
                    time[2] = (df.parse(next_point1[0]).getTime() - df.parse(cur_point[0]).getTime()) / 1000.0 + 0.1;

                    distance[2] = cal_dist(Double.parseDouble(cur_point[1]),
                            Double.parseDouble(cur_point[2]),
                            Double.parseDouble(next_point1[1]),
                            Double.parseDouble(next_point1[2]));
                    time[3] = (df.parse(next_point2[0]).getTime() - df.parse(next_point1[0]).getTime()) / 1000.0 + 0.1;

                    distance[3] = cal_dist(Double.parseDouble(next_point1[1]),
                            Double.parseDouble(next_point1[2]),
                            Double.parseDouble(next_point2[1]),
                            Double.parseDouble(next_point2[2]));

                    // 计算当前点的平均速度
                    double speed = (distance[0] + distance[1] + distance[2] + distance[3]) / (time[0] + time[1] + time[2] + time[3]);

                    // 计算下一个点的角度 345
                    double next_angle = getDegree(Double.parseDouble(next_point1[1]),
                            Double.parseDouble(next_point1[2]),
                            Double.parseDouble(cur_point[1]),
                            Double.parseDouble(cur_point[2]),
                            Double.parseDouble(next_point2[1]),
                            Double.parseDouble(next_point2[2]));
                    
                    
                    
                    System.out.print("before---------cur:"+cur_angle+"++++++++++++++++ next:"+next_angle+"\n");
                    //如果当前点和前一个点的时间间隔大于10s，继续判断
                    // 根据速度和角度判断当前点是否异常，并维护一个不异常的list
//                    if (next_angle > min_angle || cur_angle > min_angle) {
//                    if (speed < max_speed && (next_angle > min_angle || cur_angle > min_angle)) {
                    if (time[1] > 10 && speed < max_speed && ((next_angle > mid_angle || cur_angle > mid_angle) || 
                    		(cur_angle > min_angle && next_angle > min_angle)) && 
                    		!(Double.isNaN(next_angle) || Double.isNaN(cur_angle))) {
                    //如果当前角是钝角或当前角为锐角 下一个角为锐角
//                    if (time[1] > 10 && speed < max_speed && ((cur_angle > mid_angle) || 
//                    		(cur_angle > min_angle && next_angle > mid_angle)) && 
//                    		!(Double.isNaN(next_angle) || Double.isNaN(cur_angle))) {
                        //                    if (speed < max_speed ) {
                        //存入正常点中
                    	normal_index.add(i);
                    	
                    	System.out.print("after---------cur:"+cur_angle+"++++++++++++++++ next:"+next_angle+"\n");
                    	
                        time[0] = time[1];
                        time[1] = time[2];
                        distance[0] = distance[1];
                        distance[1] = distance[2];
                        cur_angle = next_angle;
//                        first_point = second_point;
                        second_point = cur_point;
                        cur_point = next_point1;
                        next_point1 = next_point2;
                    } //if
                    else {
                        time[1] = (df.parse(next_point1[0]).getTime() - df.parse(second_point[0]).getTime()) / 1000.0 + 0.1;
                        time[2] = time[3];
                        distance[1] = cal_dist(Double.parseDouble(second_point[1]),
                                Double.parseDouble(second_point[2]),
                                Double.parseDouble(next_point1[1]),
                                Double.parseDouble(next_point1[2]));
                        distance[2] = distance[3];
                        // 重新计算夹角 为点245的夹角
                        cur_angle = getDegree(Double.parseDouble(next_point1[1]),
                                Double.parseDouble(next_point1[2]),
                                Double.parseDouble(second_point[1]),
                                Double.parseDouble(second_point[2]),
                                Double.parseDouble(next_point2[1]),
                                Double.parseDouble(next_point2[2]));

                        cur_point = next_point1;
                        next_point1 = next_point2;

                    }//else
                }//for

                int normal_len = normal_index.size();
                if (normal_len > 5) {
                    // 由于前两个点无法平滑，所以将第1,2个点加入res
                    res.append(splitData[1]).append(";");
                    res.append(splitData[2]).append(";");
                    // 在指定条件下对轨迹进行平滑
                    // 如果当前的轨迹点 大于 w + w/2, 即已经有w以上个点经过处理，则利用前w个点进行平滑
                    // 将平滑后的点的信息放入最终输出当中

                    Double[] lon = new Double[5];
                    Double[] lat = new Double[5];

                    String[] point1 = splitData[normal_index.get(0)].split(",");
                    String[] point2 = splitData[normal_index.get(1)].split(",");
                    String[] point3 = splitData[normal_index.get(2)].split(",");
                    String[] point4 = splitData[normal_index.get(3)].split(",");

                    lon[0] = Double.parseDouble(point1[1]);
                    lat[0] = Double.parseDouble(point1[2]);
                    lon[1] = Double.parseDouble(point2[1]);
                    lat[1] = Double.parseDouble(point2[2]);
                    lon[2] = Double.parseDouble(point3[1]);
                    lat[2] = Double.parseDouble(point3[2]);
                    lon[3] = Double.parseDouble(point4[1]);
                    lat[3] = Double.parseDouble(point4[2]);


                    for (int i = w / 2; i < normal_len - w / 2 - 1; ++i) {
                        String[] point = splitData[normal_index.get(i + w / 2)].split(",");
                        lon[4] = Double.parseDouble(point[1]);
                        lat[4] = Double.parseDouble(point[2]);
                        // 对五个点进行排序，实际只需要对新加入的点排序
                        // 使用 a-trimmed-mean 求出均值点
                        Tuple2<Double, Double> mean_loc = trimmed_mean(lon, lat);
//                        point[1] = mean_loc._1.toString();
//                        point[2] = mean_loc._2.toString();
                        // 将此点的信息加入res
                        res.append(String.join(",", point)).append(";");
                        // 进行平移
                        lon[0] = lon[1];
                        lon[1] = lon[2];
                        lon[2] = lon[3];
                        lon[3] = lon[4];
                        lat[0] = lat[1];
                        lat[1] = lat[2];
                        lat[2] = lat[3];
                        lat[3] = lat[4];
                    }//for


                    // 添加最后两个点
                    res.append(splitData[len - 1]).append(";");
                    res.append(splitData[len]).append(";");
                } //if

                return res.toString();
			}
		});
	}
	
    /**
     * 计算两个经纬度点之间的距离
     *
     * @param cur_lon
     * @param cur_lat
     * @param previous_lon
     * @param previous_lat
     * @return 两个经纬度之间的距离
     */
    public static double cal_dist(double cur_lon, double cur_lat, double previous_lon, double previous_lat) {
        final double EARTH_RADIUS = 6378137.0;    //地球半径，单位米
        //计算经纬度差
        double lon_difference = Math.abs(rad(cur_lon) - rad(previous_lon));
        double lat_difference = Math.abs(rad(cur_lat) - rad(previous_lat));


        double distance = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(lat_difference / 2), 2)
                        + Math.cos(rad(cur_lat)) * Math.cos(rad(previous_lat)) * Math.pow(Math.sin(lon_difference / 2), 2)));
        distance = distance * EARTH_RADIUS;
        distance = Math.round(distance * 10000d) / 10000d;
        return distance;
    }//cal_dist

    /**
     * 将角度变为弧度
     *
     * @param d 输入的角度
     * @return 返回弧度值
     */
    public static double rad(double d) {
        return d * Math.PI / 180.00;
    }//rad


    public static Tuple2<Double, Double> trimmed_mean(Double[] lon, Double[] lat) {
        // 按照经纬度排序，暂时按照经度大小排序
        // 获取其最大最小值index
    	Double max=lon[0],min=lon[0];
    	for(int i=1;i<lon.length;i++){
    		max=max<lon[i]?lon[i]:max;
    		min=min>lon[i]?lon[i]:min;
    	}
    	int max_index= max==lon[0] ? 0 : max==lon[1] ? 1 : max==lon[2] ? 2 : max==lon[3] ? 3 : 4;
    	int min_index= min==lon[0] ? 0 : min==lon[1] ? 1 : min==lon[2] ? 2 : min==lon[3] ? 3 : 4;
    	
        //int max_index = IntStream.range(0, lon.length).reduce((i, j) -> lon[i] > lon[j] ? j : i).getAsInt();
        //int min_index = IntStream.range(0, lon.length).reduce((i, j) -> lon[i] < lon[j] ? j : i).getAsInt();
        // 进行 α-trimmed-mean
        Double mean_lon = (lon[0] + lon[1] + lon[2] + lon[3] + lon[4] - lon[max_index] - lon[min_index]) / 3.0;
        Double mean_lat = (lat[0] + lat[1] + lat[2] + lat[3] + lat[4] - lat[max_index] - lat[min_index]) / 3.0;

        // 返回平均后的经纬度
        return new Tuple2<Double, Double>(mean_lon, mean_lat);
    } //trimmed_mean

    /**
     * @param vertexPointX -- 角度对应顶点X坐标值
     * @param vertexPointY -- 角度对应顶点Y坐标值
     * @param point0X
     * @param point0Y
     * @param point1X
     * @param point1Y
     * @return
     */
    public static double getDegree(double vertexPointX, double vertexPointY, double point0X, double point0Y, double point1X, double point1Y) {
    	double[] pt1=wgs84toMercator(point0X,point0Y);
    	double[] pt2=wgs84toMercator(vertexPointX,vertexPointY);
    	double[] pt3=wgs84toMercator(point1X,point1Y);
    	double pt1X=pt1[0];
    	double pt2X=pt2[0];
    	double pt3X=pt3[0];
    	double pt1Y=pt1[1];
    	double pt2Y=pt2[1];
    	double pt3Y=pt3[1];
    	
        //向量的点乘
        double vector = (pt1X - pt2X) * (pt3X - pt2X) + (pt1Y - pt2Y) * (pt3Y - pt2Y);
        //向量的模乘
        double sqrt = Math.sqrt(
                (Math.abs((pt1X - pt2X) * (pt1X - pt2X)) + Math.abs((pt1Y - pt2Y) * (pt1Y - pt2Y)))
                        * (Math.abs((pt3X - pt2X) * (pt3X - pt2X)) + Math.abs((pt3Y - pt2Y) * (pt3Y - pt2Y)))
        );
        //反余弦计算弧度
        double radian = Math.acos(vector / sqrt);
        //弧度转角度制
        return 180 * radian / Math.PI;
    }
    
    /**
     * @param lon -- 经度
     * @param lat -- 纬度
     * @return
     */
    public static double[] wgs84toMercator(double lon,double lat){
    	double[] coord=new double[2];
    	coord[0]=lon*20037508.342789/180;
    	double tmp=Math.log(Math.tan((90+lat)*Math.PI/360))/(Math.PI/180);
    	coord[1]=tmp*20037508.342789/180;
    	return coord;
    }
}