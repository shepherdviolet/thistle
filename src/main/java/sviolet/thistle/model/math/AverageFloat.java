/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.model.math;

/**
 * Float平均值<p>
 * 
 * 记录最近sampleSize次数据样本，并输出平均值
 * 
 * @author S.Violet
 *
 */

public class AverageFloat {
	
	private float[] sample;//样本数据
	private int offset;//当前写入位置
	private boolean full;//样本容器是否填满
	
	/**
	 * 
	 * 无填充，起始样本数小于容器大小
	 * 
	 * @param sampleSize 样本容器大小
	 */
	public AverageFloat(int sampleSize){
		sample = new float[sampleSize];
		offset = 0;
		full = false;
	}
	
	/**
	 * 
	 * 用padding值填充满样本容器
	 * 
	 * @param sampleSize 样本容器大小
	 * @param padding 样本容器填充数据
	 */
	public AverageFloat(int sampleSize, float padding){
		this(sampleSize);
		
		for(int i = 0 ; i < sample.length ; i++){
			sample[i] = padding;
		}
		full = true;
	}
	
	/**
	 * 采样（记录一条数据）
	 * 
	 * @param data
	 * @return 返回当前平均值
	 */
	public float sampling(float data){
		sample[offset] = data;
		offset++;
		if(offset >= sample.length){
			full = true;
			offset = 0;
		}
		
		return getAverage();
	}
	
	/**
	 * 返回当前平均值
	 * 
	 * @return
	 */
	public float getAverage(){
		if(full){
			float sum = 0;
			for(int i = 0 ; i < sample.length ; i++){
				sum += sample[i];
			}
			return sum / sample.length;
		}else{
			float sum = 0;
			for(int i = 0 ; i < offset ; i++){
				sum += sample[i];
			}
			return sum / offset;
		}
	}
}
