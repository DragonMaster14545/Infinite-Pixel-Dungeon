package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;

public class Wiki {

	public static final String WIKI_FILE = "wiki.dat";

	private static final String IDS = "ids";
	private static final String TITLES = "titles";
	private static final String BODIES = "bodies";
	private static final String CATEGORIES = "categories";
	private static final String READ = "read";
	private static final String ICONS = "icons";

	public static class Article {
		public final String id;
		public String title;
		public String category;
		public int icon;
		public String body;
		public final boolean custom;

		Article(String id, String title, String category, int icon, String body, boolean custom) {
			this.id = id;
			this.title = title;
			this.category = category;
			this.icon = icon;
			this.body = body;
			this.custom = custom;
		}
	}

	private static final ArrayList<Article> customArticles = new ArrayList<>();
	private static final HashSet<String> readIds = new HashSet<>();
	private static boolean loaded = false;

	public static String defaultCategory() {
		String value = text("default_category");
		return value == null || value.trim().isEmpty() ? "General" : value;
	}

	private static String text(String key) {
		String value = Messages.get(Wiki.class, key);
		return Messages.NO_TEXT_FOUND.equals(value) ? null : value;
	}

	private static void load() {
		if (loaded) return;
		loaded = true;
		customArticles.clear();
		readIds.clear();

		Bundle bundle;
		try {
			bundle = FileUtils.bundleFromFile(WIKI_FILE);
		} catch (IOException e) {
			return;
		}

		String[] readArr = bundle.contains(READ) ? bundle.getStringArray(READ) : null;
		if (readArr != null) for (String r : readArr) readIds.add(r);

		int[] icons = bundle.contains(ICONS) ? bundle.getIntArray(ICONS) : null;
		String[] cats = bundle.contains(CATEGORIES) ? bundle.getStringArray(CATEGORIES) : null;
		String[] ids = bundle.getStringArray(IDS);
		String[] titles = bundle.getStringArray(TITLES);
		String[] bodies = bundle.getStringArray(BODIES);
		if (ids == null || titles == null || bodies == null) return;

		int n = Math.min(ids.length, Math.min(titles.length, bodies.length));
		for (int i = 0; i < n; i++) {
			String cat = cats != null && i < cats.length && cats[i] != null && !cats[i].trim().isEmpty() ? cats[i] : defaultCategory();
			int icon = icons != null && i < icons.length ? icons[i] : ItemSpriteSheet.INFO_PAGE;
			customArticles.add(new Article(ids[i], titles[i], cat, icon, bodies[i], true));
		}
	}

	public static void save() {
		load();
		int n = customArticles.size();
		String[] ids = new String[n];
		String[] titles = new String[n];
		String[] bodies = new String[n];
		String[] cats = new String[n];
		int[] icons = new int[n];
		for (int i = 0; i < n; i++) {
			Article a = customArticles.get(i);
			cats[i] = a.category;
			icons[i] = a.icon;
			ids[i] = a.id;
			titles[i] = a.title;
			bodies[i] = a.body;
		}

		Bundle bundle = new Bundle();
		bundle.put(IDS, ids);
		bundle.put(TITLES, titles);
		bundle.put(BODIES, bodies);
		bundle.put(CATEGORIES, cats);
		bundle.put(ICONS, icons);
		bundle.put(READ, readIds.toArray(new String[0]));

		try {
			FileUtils.bundleToFile(WIKI_FILE, bundle);
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	public static boolean isRead(Article article) {
		load();
		return readIds.contains(article.id);
	}

	public static void markRead(Article article) {
		load();
		if (readIds.add(article.id)) save();
	}

	public static Article add(String title, String category, int icon, String body) {
		load();
		Article a = new Article("custom_" + System.currentTimeMillis() + "_" + customArticles.size(),
				title, category == null || category.trim().isEmpty() ? defaultCategory() : category.trim(), icon, body, true);
		customArticles.add(a);
		readIds.add(a.id);
		save();
		return a;
	}

	public static void update(Article article, String title, String category, Integer icon, String body) {
		if (article == null || !article.custom) return;
		if (title != null) article.title = title;
		if (category != null) article.category = category.trim().isEmpty() ? defaultCategory() : category.trim();
		if (icon != null) article.icon = icon;
		if (body != null) article.body = body;
		save();
	}

	public static LinkedHashMap<String, ArrayList<Article>> grouped(String query) {
		LinkedHashMap<String, ArrayList<Article>> map = new LinkedHashMap<>();
		for (Article a : search(query)) {
			ArrayList<Article> list = map.get(a.category);
			if (list == null) {
				list = new ArrayList<>();
				map.put(a.category, list);
			}
			list.add(a);
		}
		return map;
	}

	public static void remove(Article article) {
		load();
		if (article != null && customArticles.remove(article)) save();
	}

	private static TreeMap<String, Integer> spriteNames = null;

	public static TreeMap<String, Integer> spriteNames() {
		if (spriteNames != null) return spriteNames;
		TreeMap<String, Integer> map = new TreeMap<>();
		try {
			for (Field f : ItemSpriteSheet.class.getFields()) {
				int mod = f.getModifiers();
				if (f.getType() != int.class || !Modifier.isStatic(mod) || !Modifier.isFinal(mod)) continue;
				String name = f.getName();
				if (name.equals("SIZE") || name.equals("WIDTH")) continue;
				int value = f.getInt(null);
				if (value < 0 || ItemSpriteSheet.film.get(value) == null) continue;
				map.put(name, value);
			}
		} catch (Exception e) {
		}
		spriteNames = map;
		return map;
	}

	public static int spriteByName(String name) {
		if (name == null) return ItemSpriteSheet.INFO_PAGE;
		Integer v = spriteNames().get(name.trim().toUpperCase(Locale.ENGLISH));
		return v == null ? ItemSpriteSheet.INFO_PAGE : v;
	}

	public static ArrayList<Article> articles() {
		load();
		ArrayList<Article> out = new ArrayList<>();

		String list = text("articles");
		if (list != null) {
			for (String raw : list.split(",")) {
				String id = raw.trim().toLowerCase(Locale.ENGLISH);
				if (id.isEmpty()) continue;
				String title = text(id + "_title");
				String body = text(id + "_body");
				if (title == null && body == null) continue;
				String cat = text(id + "_category");
				String iconName = text(id + "_icon");
				int icon = iconName == null ? ItemSpriteSheet.INFO_PAGE : spriteByName(iconName);
				out.add(new Article(id, title == null ? id : title, cat == null ? defaultCategory() : cat, icon, body == null ? "" : body, false));
			}
		}

		out.addAll(customArticles);
		return out;
	}

	public static ArrayList<Article> search(String query) {
		ArrayList<Article> all = articles();
		if (query == null || query.trim().isEmpty()) return all;

		String[] words = query.trim().toLowerCase(Locale.ENGLISH).split("\\s+");
		ArrayList<Article> out = new ArrayList<>();
		for (Article article : all) {
			String haystack = (article.title + " " + article.category + " " + article.body).toLowerCase(Locale.ENGLISH);
			boolean ok = true;
			for (String word : words) {
				if (!haystack.contains(word)) {
					ok = false;
					break;
				}
			}
			if (ok) out.add(article);
		}
		return out;
	}
}
