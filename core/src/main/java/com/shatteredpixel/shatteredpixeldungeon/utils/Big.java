package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class Big {

	private Big(){}

	public static final MathContext MC = MathContext.DECIMAL128;
	private static final BigInteger LMIN = BigInteger.valueOf(Long.MIN_VALUE);
	private static final BigInteger LMAX = BigInteger.valueOf(Long.MAX_VALUE);

	public static BigInteger of(long v){ return BigInteger.valueOf(v); }

	
	public static BigInteger of(double v){
		if (Double.isNaN(v)) return BigInteger.ZERO;
		if (Double.isInfinite(v)) return v > 0 ? LMAX : LMIN;
		return new BigDecimal(v).toBigInteger();
	}
	public static BigInteger of(float v){ return of((double) v); }

	public static BigInteger roundD(double d){
		if (Double.isNaN(d)) return BigInteger.ZERO;
		return of(Math.floor(d + 0.5));
	}

	public static float ratio(BigInteger a, BigInteger b){
		if (b.signum() == 0) return 0f;
		return new BigDecimal(a).divide(new BigDecimal(b), MC).floatValue();
	}

	private static final String[] SUF = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc", "Ud", "Dd", "Td", "Qad", "Qid", "Sxd", "Spd", "Ocd", "Nod", "Vg"};

	public static String fmt(BigInteger v){
		if (v == null) return "0";
		String sign = v.signum() < 0 ? "-" : "";
		String s = v.abs().toString();
		int len = s.length();
		if (len <= 4) return sign + s;
		int idx = (len - 1) / 3;
		String m;
		String suffix;
		if (idx < SUF.length){
			int lead = len - 3 * idx;
			m = s.substring(0, lead);
			int dec = Math.max(0, 3 - lead);
			if (dec > 0){
				String f = s.substring(lead, lead + dec).replaceAll("0+$", "");
				if (!f.isEmpty()) m += "." + f;
			}
			suffix = SUF[idx];
		} else {
			m = s.substring(0, 1) + "." + s.substring(1, 3);
			suffix = "e" + (len - 1);
		}
		return sign + m + suffix;
	}

	private static final java.util.regex.Pattern NUM = java.util.regex.Pattern.compile("\\d{5,}");

	public static String fmtText(String t){
		if (t == null || t.length() < 5) return t;
		java.util.regex.Matcher m = NUM.matcher(t);
		StringBuffer sb = new StringBuffer();
		while (m.find()) m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(fmt(new BigInteger(m.group()))));
		m.appendTail(sb);
		return sb.toString();
	}

	public static BigInteger parse(String t){
		t = t.trim();
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("([+-]?)(\\d*\\.?\\d+)([A-Za-z]*)").matcher(t);
		if (!m.matches()) throw new NumberFormatException(t);
		BigDecimal v = new BigDecimal(m.group(2));
		String suf = m.group(3);
		if (!suf.isEmpty()){
			int idx = -1;
			for (int i = 1; i < SUF.length; i++) if (SUF[i].equalsIgnoreCase(suf)) { idx = i; break; }
			if (idx > 0) v = v.scaleByPowerOfTen(3 * idx);
			else throw new NumberFormatException(t);
		}
		BigInteger r = v.toBigInteger();
		return m.group(1).equals("-") ? r.negate() : r;
	}

	public static BigInteger trunc(BigDecimal d){ return d.toBigInteger(); }

	
	public static BigInteger round(BigDecimal d){
		return d.add(new BigDecimal("0.5")).setScale(0, RoundingMode.FLOOR).toBigInteger();
	}
	public static BigInteger round(BigInteger b){ return b; }

	public static BigDecimal ceil(BigDecimal d){ return d.setScale(0, RoundingMode.CEILING); }
	public static BigDecimal floor(BigDecimal d){ return d.setScale(0, RoundingMode.FLOOR); }
	public static BigDecimal ceil(BigInteger b){ return new BigDecimal(b); }
	public static BigDecimal floor(BigInteger b){ return new BigDecimal(b); }

	public static BigDecimal dec(BigInteger b){ return new BigDecimal(b); }
	public static BigDecimal dec(BigDecimal b){ return b; }
	public static BigDecimal dec(long v){ return BigDecimal.valueOf(v); }
	public static BigDecimal dec(int v){ return BigDecimal.valueOf(v); }
	public static BigDecimal dec(double v){
		if (Double.isNaN(v) || Double.isInfinite(v)) return BigDecimal.ZERO;
		return BigDecimal.valueOf(v);
	}
	public static BigDecimal dec(float v){
		if (Float.isNaN(v) || Float.isInfinite(v)) return BigDecimal.ZERO;
		return new BigDecimal(Float.toString(v));
	}

	public static BigDecimal div(BigDecimal a, BigDecimal b){
		if (b.signum() == 0) return a.signum() == 0 ? BigDecimal.ZERO : new BigDecimal(a.signum() > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE);
		return a.divide(b, MC);
	}

	
	public static long toLong(BigInteger b){
		if (b.compareTo(LMAX) > 0) return Long.MAX_VALUE;
		if (b.compareTo(LMIN) < 0) return Long.MIN_VALUE;
		return b.longValue();
	}
	
	public static int toInt(BigInteger b){
		if (b.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) return Integer.MAX_VALUE;
		if (b.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) return Integer.MIN_VALUE;
		return b.intValue();
	}
	public static int toInt(BigDecimal b){ return toInt(b.toBigInteger()); }

	public static boolean fitsLong(BigInteger b){ return b.bitLength() < 64; }

	public static BigInteger[] newArray(int n){
		BigInteger[] a = new BigInteger[n];
		java.util.Arrays.fill(a, BigInteger.ZERO);
		return a;
	}

	public static BigInteger getBig(Bundle b, String key){
		if (!b.contains(key)) return BigInteger.ZERO;
		String s = b.getString(key);
		if (s == null || s.isEmpty()) return BigInteger.ZERO;
		try { return new BigInteger(s.trim()); }
		catch (NumberFormatException e){
			try { return new BigDecimal(s.trim()).toBigInteger(); }
			catch (NumberFormatException e2){ return BigInteger.ZERO; }
		}
	}
	public static void put(Bundle b, String key, BigInteger v){
		b.put(key, v == null ? "0" : v.toString());
	}
	public static BigInteger[] getBigArray(Bundle b, String key){
		if (!b.contains(key)) return new BigInteger[0];
		String[] s = b.getStringArray(key);
		if (s == null) return new BigInteger[0];
		BigInteger[] out = new BigInteger[s.length];
		for (int i = 0; i < s.length; i++){
			try { out[i] = new BigInteger(s[i].trim()); } catch (Exception e){ out[i] = BigInteger.ZERO; }
		}
		return out;
	}
	public static void putArray(Bundle b, String key, BigInteger[] v){
		String[] s = new String[v.length];
		for (int i = 0; i < v.length; i++) s[i] = v[i] == null ? "0" : v[i].toString();
		b.put(key, s);
	}

	private static BigInteger uniform(BigInteger bound){
		if (bound.signum() <= 0) return BigInteger.ZERO;
		if (fitsLong(bound)) return BigInteger.valueOf(Random.Long(bound.longValue()));
		int bits = bound.bitLength();
		BigInteger r;
		do {
			r = BigInteger.ZERO;
			for (int i = 0; i < bits; i += 62){
				r = r.shiftLeft(62).or(BigInteger.valueOf(Random.Long(1L << 62)));
			}
			r = r.shiftRight(((bits + 61) / 62) * 62 - bits);
		} while (r.compareTo(bound) >= 0);
		return r;
	}

	
	public static BigInteger range(BigInteger min, BigInteger max){
		if (fitsLong(min) && fitsLong(max)) return BigInteger.valueOf(Random.LongRange(min.longValue(), max.longValue()));
		if (max.compareTo(min) < 0) return min;
		return min.add(uniform(max.subtract(min).add(BigInteger.ONE)));
	}

	
	public static BigInteger normalRange(BigInteger min, BigInteger max){
		if (fitsLong(min) && fitsLong(max) && fitsLong(max.subtract(min))) {
			return BigInteger.valueOf(Random.NormalLongRange(min.longValue(), max.longValue()));
		}
		if (max.compareTo(min) < 0) return min;
		BigInteger span = max.subtract(min);
		BigInteger a = uniform(span.add(BigInteger.ONE));
		BigInteger b = uniform(span.add(BigInteger.ONE));
		return min.add(a.add(b).shiftRight(1));
	}

	public static BigInteger invNormalRange(BigInteger min, BigInteger max){
		if (fitsLong(min) && fitsLong(max) && fitsLong(max.subtract(min))) {
			return BigInteger.valueOf(Random.InvNormalLongRange(min.longValue(), max.longValue()));
		}
		return range(min, max);
	}
}
