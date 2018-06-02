package com.flipboard.bottomsheet.commons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import flipboard.bottomsheet.commons.R;

import static com.flipboard.bottomsheet.commons.MenuSheetView.MenuType.GRID;
import static com.flipboard.bottomsheet.commons.MenuSheetView.MenuType.LIST;

/**
 * A SheetView that can represent a menu resource as a list or grid.
 * <p>
 * A list can support submenus, and will include a divider and header for them where appropriate.
 * Grids currently don't support submenus, and don't in the Material Design spec either.
 * </p>
 */
@SuppressLint("ViewConstructor")
public class MenuSheetView extends FrameLayout {

    public static final int DEFAULT_LAYOUT_LIST_ITEM = R.layout.sheet_list_item;
    public static final int DEFAULT_LAYOUT_GRID_ITEM = R.layout.sheet_grid_item;

    /**
     * A listener for menu item clicks in the sheet
     */
    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem item);
    }

    /**
     * The supported display types for the menu items.
     */
    public enum MenuType {LIST, GRID}

    private Menu menu;
    private final MenuType menuType;
    private ArrayList<SheetMenuItem> items = new ArrayList<>();
    private Adapter adapter;
    private AbsListView absListView;
    private final TextView titleView;
    private int columnWidthDp = 100;
    private int listItemLayoutRes = DEFAULT_LAYOUT_LIST_ITEM;
    private int gridItemLayoutRes = DEFAULT_LAYOUT_GRID_ITEM;

    /**
     * @param context Context to construct the view with
     * @param menuType LIST or GRID
     * @param titleRes String resource ID for the title
     * @param listener Listener for menu item clicks in the sheet
     */
    public MenuSheetView(final Context context, final MenuType menuType, @StringRes final int titleRes, final OnMenuItemClickListener listener) {
        this(context, menuType, context.getString(titleRes), listener);
    }

    /**
     * @param context Context to construct the view with
     * @param menuType LIST or GRID
     * @param title Title for the sheet. Can be null
     * @param listener Listener for menu item clicks in the sheet
     */
    public MenuSheetView(final Context context, final MenuType menuType, @Nullable final CharSequence title, final OnMenuItemClickListener listener) {
        super(context);

        // Set up the menu
        this.menu = new MenuBuilder(context);

        SupportMenuInflater inflater = new SupportMenuInflater(getContext());
        inflater.inflate(R.menu.create, menu);
        prepareMenuItems();
        this.menuType = menuType;

        // Inflate the appropriate view and set up the absListView
        inflate(context, menuType == GRID ? R.layout.grid_sheet_view : R.layout.list_sheet_view, this);
        absListView = (AbsListView) findViewById(menuType == GRID ? R.id.grid : R.id.list);
        if (listener != null) {
            absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.onMenuItemClick(adapter.getItem(position).getMenuItem());
                }
            });
        }

        // Set up the title
        titleView = (TextView) findViewById(R.id.title);
        setTitle(title);

        ViewCompat.setElevation(this, Util.dp2px(getContext(), 16f));
    }

    /**
     * Inflates a menu resource into the menu backing this sheet.
     *
     * @param menuRes Menu resource ID
     */
    public void inflateMenu(@MenuRes int menuRes) {
        if (menuRes != -1) {
            SupportMenuInflater inflater = new SupportMenuInflater(getContext());
            inflater.inflate(menuRes, menu);
        }

        prepareMenuItems();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.adapter = new Adapter();
        absListView.setAdapter(this.adapter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ((GridView) absListView).setNumColumns(4);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Necessary for showing elevation on 5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new Util.ShadowOutline(w, h));
        }
    }

    /**
     * Flattens the visible menu items of {@link #menu} into {@link #items},
     * while inserting separators between items when necessary.
     *
     * Adapted from the Design support library's NavigationMenuPresenter implementation
     */
    private void prepareMenuItems() {
        items.clear();
        int currentGroupId = 0;

        // Iterate over the menu items
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isVisible()) {
                if (item.hasSubMenu()) {
                    // Flatten the submenu
                    SubMenu subMenu = item.getSubMenu();
                    if (subMenu.hasVisibleItems()) {
                        if (menuType == LIST) {
                            items.add(SheetMenuItem.SEPARATOR);

                            // Add a header item if it has text
                            if (!TextUtils.isEmpty(item.getTitle())) {
                                items.add(SheetMenuItem.of(item));
                            }
                        }

                        // Add the sub-items
                        for (int subI = 0, size = subMenu.size(); subI < size; subI++) {
                            MenuItem subMenuItem = subMenu.getItem(subI);
                            if (subMenuItem.isVisible()) {
                                items.add(SheetMenuItem.of(subMenuItem));
                            }
                        }

                        // Add one more separator to the end to close it off if we have more items
                        if (menuType == LIST && i != menu.size() - 1) {
                            items.add(SheetMenuItem.SEPARATOR);
                        }
                    }
                } else {
                    int groupId = item.getGroupId();
                    if (groupId != currentGroupId && menuType == LIST) {
                        items.add(SheetMenuItem.SEPARATOR);
                    }
                    items.add(SheetMenuItem.of(item));
                    currentGroupId = groupId;
                }
            }
        }
    }

    /**
     * @return The current {@link Menu} instance backing this sheet. Note that this is mutable, and
     *         you should call {@link #updateMenu()} after any changes.
     */
    public Menu getMenu() {
        return this.menu;
    }

    /**
     * Invalidates the current internal representation of the menu and rebuilds it. Should be used
     * if the developer dynamically adds items to the Menu returned by {@link #getMenu()}
     */
    public void updateMenu() {
        // Invalidate menu and rebuild it, useful if the user has dynamically added menu items.
        prepareMenuItems();
    }

    /**
     * @return The {@link MenuType} this instance was built with
     */
    public MenuType getMenuType() {
        return this.menuType;
    }

    /**
     * Sets the title text of the sheet
     *
     * @param resId String resource ID for the text
     */
    public void setTitle(@StringRes int resId) {
        setTitle(getResources().getText(resId));
    }

    /**
     * Sets the title text of the sheet
     *
     * @param title Title text to use
     */
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            titleView.setText(title);
        } else {
            titleView.setVisibility(GONE);
        }
    }

    /**
     * Only applies to GRID
     */
    public void setColumnWidthDp(int columnWidthDp) {
        this.columnWidthDp = columnWidthDp;
    }

    /**
     * Override the layout for displaying a list item when of type {@link MenuType#LIST}. <br>
     * Call this before you attach to window using {@link com.flipboard.bottomsheet.BottomSheetLayout#showWithSheetView}.
     * @param listItemLayoutRes needs to have an {@link ImageView} with id set to {@link R.id#icon} and {@link TextView} with id set to {@link R.id#label}
     */
    public void setListItemLayoutRes(@LayoutRes int listItemLayoutRes) {
        this.listItemLayoutRes = listItemLayoutRes;
    }

    /**
     * Override the layout for displaying a grid item when of type {@link MenuType#GRID}. <br>
     * Call this before you attach to window using {@link com.flipboard.bottomsheet.BottomSheetLayout#showWithSheetView}.
     * @param gridItemLayoutRes needs to have an {@link ImageView} with id set to {@link R.id#icon} and {@link TextView} with id set to {@link R.id#label}
     */
    public void setGridItemLayoutRes(@LayoutRes int gridItemLayoutRes) {
        this.gridItemLayoutRes = gridItemLayoutRes;
    }


    /**
     * @return The current title text of the sheet
     */
    public CharSequence getTitle() {
        return titleView.getText();
    }

    private class Adapter extends BaseAdapter {

        private static final int VIEW_TYPE_NORMAL = 0;
        private static final int VIEW_TYPE_SUBHEADER = 1;
        private static final int VIEW_TYPE_SEPARATOR = 2;

        private final LayoutInflater inflater;

        public Adapter() {
            this.inflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public SheetMenuItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            SheetMenuItem item = getItem(position);
            if (item.isSeparator()) {
                return VIEW_TYPE_SEPARATOR;
            } else if (item.getMenuItem().hasSubMenu()) {
                return VIEW_TYPE_SUBHEADER;
            } else {
                return VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SheetMenuItem item = getItem(position);
            int viewType = getItemViewType(position);

            switch (viewType) {
                case VIEW_TYPE_NORMAL:
                    NormalViewHolder holder;
                    if (convertView == null) {
                        convertView = inflater.inflate(menuType == GRID ? gridItemLayoutRes : listItemLayoutRes, parent, false);
                        holder = new NormalViewHolder(convertView);
                        convertView.setTag(holder);
                    } else {
                        holder = (NormalViewHolder) convertView.getTag();
                    }
                    holder.bindView(item);
                    break;
                case VIEW_TYPE_SUBHEADER:
                    if (convertView == null) {
                        convertView = inflater.inflate(R.layout.sheet_list_item_subheader, parent, false);
                    }
                    TextView subHeader = (TextView) convertView;
                    subHeader.setText(item.getMenuItem().getTitle());
                    break;
                case VIEW_TYPE_SEPARATOR:
                    if (convertView == null) {
                        convertView = inflater.inflate(R.layout.sheet_list_item_separator, parent, false);
                    }
                    break;
            }

            return convertView;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        class NormalViewHolder {
            final ImageView icon;

            NormalViewHolder(View root) {
                icon = (ImageView) root.findViewById(R.id.icon);
            }

            public void bindView(SheetMenuItem item) {
                if (item.getMenuItem().getItemId()==R.id.s1) {
                    Glide.with(getContext())
                            .load(R.drawable.s1)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s2) {
                    Glide.with(getContext())
                            .load(R.drawable.s2)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s3) {
                    Glide.with(getContext())
                            .load(R.drawable.s3)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s4) {
                    Glide.with(getContext())
                            .load(R.drawable.s4)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s5) {
                    Glide.with(getContext())
                            .load(R.drawable.s5)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s6) {
                    Glide.with(getContext())
                            .load(R.drawable.s6)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s7) {
                    Glide.with(getContext())
                            .load(R.drawable.s7)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s8) {
                    Glide.with(getContext())
                            .load(R.drawable.s8)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s9) {
                    Glide.with(getContext())
                            .load(R.drawable.s9)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s10) {
                    Glide.with(getContext())
                            .load(R.drawable.s10)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s11) {
                    Glide.with(getContext())
                            .load(R.drawable.s11)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s12) {
                    Glide.with(getContext())
                            .load(R.drawable.s12)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s13) {
                    Glide.with(getContext())
                            .load(R.drawable.s13)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s14) {
                    Glide.with(getContext())
                            .load(R.drawable.s14)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s15) {
                    Glide.with(getContext())
                            .load(R.drawable.s15)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s16) {
                    Glide.with(getContext())
                            .load(R.drawable.s16)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s17) {
                    Glide.with(getContext())
                            .load(R.drawable.s17)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s18) {
                    Glide.with(getContext())
                            .load(R.drawable.s18)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s19) {
                    Glide.with(getContext())
                            .load(R.drawable.s19)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s20) {
                    Glide.with(getContext())
                            .load(R.drawable.s20)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s21) {
                    Glide.with(getContext())
                            .load(R.drawable.s21)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s22) {
                    Glide.with(getContext())
                            .load(R.drawable.s22)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s23) {
                    Glide.with(getContext())
                            .load(R.drawable.s23)
                            .fitCenter()
                            .into(icon);
                }
                if (item.getMenuItem().getItemId()==R.id.s24) {
                    Glide.with(getContext())
                            .load(R.drawable.s24)
                            .fitCenter()
                            .into(icon);
                }
            }
        }
    }

    private static class SheetMenuItem {

        private static final SheetMenuItem SEPARATOR = new SheetMenuItem(null);

        private final MenuItem menuItem;

        private SheetMenuItem(MenuItem item) {
            menuItem = item;
        }

        public static SheetMenuItem of(MenuItem item) {
            return new SheetMenuItem(item);
        }

        public boolean isSeparator() {
            return this == SEPARATOR;
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public boolean isEnabled() {
            // Separators and subheaders never respond to click
            return menuItem != null && !menuItem.hasSubMenu() && menuItem.isEnabled();
        }

    }
}