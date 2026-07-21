const __vite__mapDeps = (
  i,
  m = __vite__mapDeps,
  d = m.f ||
    (m.f = [
      'assets/react-vendor-CT9J5WVz.js',
      'assets/rolldown-runtime-BgaNhQyE.js',
      'assets/AccessibilityProvider-B4x-60D-.js',
      'assets/ErrorBoundary-CliOivNv.js',
      'assets/CRFRenderer-BpsNx76v.js',
      'assets/CRFRenderer-BFjRQvKa.css',
      'assets/Navigation-BH5YU9Y7.js',
      'assets/Navigation-DndVaItp.css',
    ])
) => i.map((i) => d[i]);
import {
  i as __toESM,
  n as __esmMin,
  t as __commonJSMin,
} from './rolldown-runtime-BgaNhQyE.js';
import {
  a as jQuery$1,
  i as init_jquery_module,
  n as init_jquery_migrate_module,
  o as init_scriptaculous,
  r as init_jquery_tmpl,
  s as require_prototype,
  t as init_jquery_blockUI,
} from './legacy-vendor-CIKnzzFs.js';
import {
  a as dateFormat,
  c as _,
  i as init_ua_parser,
  l as init_index_default,
  n as init_head_min,
  o as init_dateformat,
  r as UAParser,
  s as init_index_all,
  t as require_json3,
} from './vendor-BkOcdPCZ.js';
var require_setup_globals = __commonJSMin(() => {
    (init_jquery_module(),
      init_index_all(),
      init_dateformat(),
      init_ua_parser(),
      (window.jQuery = jQuery$1),
      (window._ = _),
      (window.dateFormat = dateFormat),
      (window.UAParser = UAParser));
  }),
  require_blockui_a11y = __commonJSMin(() => {
    (init_jquery_module(),
      (function (e) {
        if (!e) return;
        let t = null,
          r = e.blockUI,
          a = e.unblockUI,
          o = e.fn.block;
        e.fn.unblock;
        function c(r) {
          t = document.activeElement;
          let a = r && r.onBlock,
            o = r && r.onUnblock,
            c = e.extend({}, r);
          return (
            (c.onBlock = function () {
              let t = e(`.blockUI.blockMsg`);
              if (t.length) {
                t.attr({ role: `dialog`, 'aria-modal': `true` });
                let r = `a[href], area[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, [tabindex]:not([tabindex="-1"]), [contenteditable]`,
                  a = t.find(r).filter(`:visible`);
                (a.length > 0
                  ? a.first().focus()
                  : t.attr(`tabindex`, `-1`).focus(),
                  e(document).on(`keydown.blockUI_a11y`, function (t) {
                    if (t.key === `Escape` || t.keyCode === 27)
                      (t.preventDefault(), t.stopPropagation(), e.unblockUI());
                    else if (t.key === `Tab` || t.keyCode === 9) {
                      let a = e(`.blockUI.blockMsg`).find(r).filter(`:visible`);
                      if (a.length === 0) {
                        t.preventDefault();
                        return;
                      }
                      let o = a[0],
                        c = a[a.length - 1];
                      (t.shiftKey
                        ? (document.activeElement === o ||
                            document.activeElement ===
                              e(`.blockUI.blockMsg`)[0]) &&
                          (c.focus(), t.preventDefault())
                        : document.activeElement === c &&
                          (o.focus(), t.preventDefault()),
                        !e(`.blockUI.blockMsg`)[0].contains(
                          document.activeElement
                        ) &&
                          document.activeElement !==
                            e(`.blockUI.blockMsg`)[0] &&
                          (o.focus(), t.preventDefault()));
                    }
                  }));
              }
              a && a.apply(this, arguments);
            }),
            (c.onUnblock = function () {
              if ((e(document).off(`keydown.blockUI_a11y`), t))
                try {
                  t.focus();
                } catch {}
              o && o.apply(this, arguments);
            }),
            c
          );
        }
        (r &&
          ((e.blockUI = function (e) {
            return r.call(this, c(e || {}));
          }),
          e.extend(e.blockUI, r)),
          a &&
            (e.unblockUI = function (e) {
              return a.call(this, e);
            }),
          o &&
            (e.fn.block = function (e) {
              return o.call(this, c(e || {}));
            }));
      })(jQuery$1));
  }),
  store,
  initialStudyOID,
  init_store = __esmMin(() => {
    ((store = {
      state: {
        studyOID: window.app_studyOID || ``,
        userSession: window.app_userSession || ``,
        formData: {},
        errors: {},
      },
      listeners: [],
      setState(e) {
        ((this.state = { ...this.state, ...e }),
          this.listeners.forEach((e) => e(this.state)));
      },
      setFormData(e, t, r, a) {
        let o = { ...this.state.formData };
        (o[e] || (o[e] = []),
          (o[e] = [...o[e]]),
          o[e][t] ? (o[e][t] = { ...o[e][t] }) : (o[e][t] = {}),
          (o[e][t][r] = a),
          this.setState({ formData: o }));
      },
      addRow(e, t) {
        let r = { ...this.state.formData };
        r[e] ? (r[e] = [...r[e]]) : (r[e] = []);
        let a = {},
          o = t.groups.find((t) => t.groupOID === e);
        (o &&
          o.fields.forEach((e) => {
            a[e.fieldOID] = ``;
          }),
          r[e].push(a),
          this.setState({ formData: r }));
      },
      removeRow(e, t) {
        let r = { ...this.state.formData };
        r[e] &&
          ((r[e] = r[e].filter((e, r) => r !== t)),
          this.setState({ formData: r }));
      },
      subscribe(e) {
        return (
          this.listeners.push(e),
          () => {
            this.listeners = this.listeners.filter((t) => t !== e);
          }
        );
      },
      getState() {
        return this.state;
      },
    }),
      (initialStudyOID = window.app_studyOID),
      initialStudyOID !== void 0 &&
        store.setState({ studyOID: initialStudyOID }),
      Object.defineProperty(window, 'app_studyOID', {
        get() {
          return store.getState().studyOID;
        },
        set(e) {
          store.getState().studyOID !== e && store.setState({ studyOID: e });
        },
        configurable: !0,
      }));
  }),
  require_jquery_jmesa = __commonJSMin(() => {
    (function ($) {
      var tableFacades = {},
        getFormByTableId = function (e) {
          for (var t = document.getElementById(e), r = !1; !r;) {
            if (t.nodeName == `FORM`) return ((r = !0), t);
            t = t.parentNode;
          }
          return null;
        },
        coreapi = {
          addTableFacade: function (e) {
            var t = new classes.TableFacade(e);
            tableFacades[t.limit.id] = t;
          },
          getTableFacade: function (e) {
            return tableFacades[e];
          },
          setSaveToWorksheet: function (e) {
            this.getTableFacade(e).worksheet.save = `true`;
          },
          setFilterToWorksheet: function (e) {
            ((this.getTableFacade(e).worksheet.filter = `true`),
              this.setPageToLimit(e, `1`));
          },
          removeFilterFromWorksheet: function (e) {
            ((this.getTableFacade(e).worksheet.filter = null),
              this.setPageToLimit(e, `1`));
          },
          setPageToLimit: function (e, t) {
            this.getTableFacade(e).limit.setPage(t);
          },
          setMaxRowsToLimit: function (e, t) {
            (this.getTableFacade(e).limit.setMaxRows(t),
              this.setPageToLimit(e, `1`));
          },
          addSortToLimit: function (e, t, r, a) {
            (this.removeAllSortsFromLimit(e), this.removeSortFromLimit(e, r));
            var o = this.getTableFacade(e).limit,
              c = new classes.Sort(t, r, a);
            o.addSort(c);
          },
          removeSortFromLimit: function (e, t) {
            var r = this.getTableFacade(e).limit.getSortSet();
            $.each(r, function (e, a) {
              if (a.property == t) return (r.splice(e, 1), !1);
            });
          },
          removeAllSortsFromLimit: function (e) {
            (this.getTableFacade(e).limit.setSortSet([]),
              this.setPageToLimit(e, `1`));
          },
          getSortFromLimit: function (e, t) {
            var r = this.getTableFacade(e).limit.getSortSet();
            $.each(r, function (e, r) {
              if (r.property == t) return r;
            });
          },
          addFilterToLimit: function (e, t, r) {
            this.removeFilterFromLimit(e, t);
            var a = this.getTableFacade(e).limit,
              o = new classes.Filter(t, r);
            a.addFilter(o);
          },
          removeFilterFromLimit: function (e, t) {
            var r = this.getTableFacade(e).limit.getFilterSet();
            $.each(r, function (e, a) {
              if (a.property == t) return (r.splice(e, 1), !1);
            });
          },
          removeSpecifiedFilterFromLimit: function (e, t, r) {
            var a = this.getTableFacade(e).limit.getFilterSet();
            $.each(a, function (e, o) {
              if (o.property.substr(0, 4) == r && o.property != t)
                return (a.splice(e, 1), !1);
            });
          },
          removeAllFiltersFromLimit: function (e) {
            var t = this.getTableFacade(e);
            (t.limit.setFilterSet([]), this.setPageToLimit(e, `1`));
            var r = t.worksheet;
            r.filter = null;
          },
          getFilterFromLimit: function (e, t) {
            var r = this.getTableFacade(e).limit.getFilterSet();
            $.each(r, function (e, r) {
              if (r.property == t) return r;
            });
          },
          setExportToLimit: function (e, t) {
            this.getTableFacade(e).limit.setExport(t);
          },
          createHiddenInputFieldsForLimit: function (e) {
            var t = this.getTableFacade(e),
              r = getFormByTableId(e);
            t.createHiddenInputFields(r);
          },
          createHiddenInputFieldsForLimitAndSubmit: function (e) {
            var t = this.getTableFacade(e),
              r = getFormByTableId(e);
            t.createHiddenInputFields(r) && r.submit();
          },
          createParameterStringForLimit: function (e) {
            return this.getTableFacade(e).createParameterString();
          },
          setOnInvokeAction: function (e, t) {
            var r = this.getTableFacade(e);
            r.onInvokeAction = t;
          },
          setOnInvokeExportAction: function (e, t) {
            var r = this.getTableFacade(e);
            r.onInvokeExportAction = t;
          },
          onInvokeAction: function (e, t) {
            var r = this.getTableFacade(e),
              a = window[r.onInvokeAction];
            if ($.isFunction(a) !== !0)
              throw r.onInvokeAction + ` is not a global function!`;
            a(e, t);
          },
          onInvokeExportAction: function (e) {
            var t = this.getTableFacade(e),
              r = window[t.onInvokeExportAction];
            if ($.isFunction(r) !== !0)
              throw t.onInvokeExportAction + ` is not a global function!`;
            r(e);
          },
        },
        classes = {
          TableFacade: function (e) {
            ((this.limit = new classes.Limit(e)),
              (this.worksheet = new classes.Worksheet()),
              (this.onInvokeAction = `onInvokeAction`),
              (this.onInvokeExportAction = `onInvokeExportAction`));
          },
          Worksheet: function () {
            ((this.save = null), (this.filter = null));
          },
          Limit: function (e) {
            ((this.id = e),
              (this.page = null),
              (this.maxRows = null),
              (this.sortSet = []),
              (this.filterSet = []),
              (this.exportType = null));
          },
          Sort: function (e, t, r) {
            ((this.position = e), (this.property = t), (this.order = r));
          },
          Filter: function (e, t) {
            ((this.property = e), (this.value = t));
          },
          DynFilter: function (e, t, r) {
            ((this.filter = e), (this.id = t), (this.property = r));
          },
          WsColumn: function (e, t, r, a) {
            ((this.column = e),
              (this.id = t),
              (this.uniqueProperties = r),
              (this.property = a));
          },
        };
      ($.extend(classes.Limit.prototype, {
        getId: function () {
          return this.id;
        },
        setId: function (e) {
          this.id = e;
        },
        getPage: function () {
          return this.page;
        },
        setPage: function (e) {
          this.page = e;
        },
        getMaxRows: function () {
          return this.maxRows;
        },
        setMaxRows: function (e) {
          this.maxRows = e;
        },
        getSortSet: function () {
          return this.sortSet;
        },
        addSort: function (e) {
          this.sortSet[this.sortSet.length] = e;
        },
        setSortSet: function (e) {
          this.sortSet = e;
        },
        getFilterSet: function () {
          return this.filterSet;
        },
        addFilter: function (e) {
          this.filterSet[this.filterSet.length] = e;
        },
        setFilterSet: function (e) {
          this.filterSet = e;
        },
        getExport: function () {
          return this.exportType;
        },
        setExport: function (e) {
          this.exportType = e;
        },
      }),
        $.extend(classes.TableFacade.prototype, {
          createHiddenInputFields: function (e) {
            var t = this.limit;
            if (
              $(e)
                .find(`:hidden[name=` + t.id + `_p_]`)
                .val()
            )
              return !1;
            (this.worksheet.save &&
              $(e).append(
                `<input type="hidden" name="` + t.id + `_sw_" value="true"/>`
              ),
              this.worksheet.filter &&
                $(e).append(
                  `<input type="hidden" name="` + t.id + `_fw_" value="true"/>`
                ),
              $(e).append(
                `<input type="hidden" name="` + t.id + `_tr_" value="true"/>`
              ),
              $(e).append(
                `<input type="hidden" name="` +
                  t.id +
                  `_p_" value="` +
                  t.page +
                  `"/>`
              ),
              $(e).append(
                `<input type="hidden" name="` +
                  t.id +
                  `_mr_" value="` +
                  t.maxRows +
                  `"/>`
              ));
            var r = t.getSortSet();
            $.each(r, function (r, a) {
              $(e).append(
                `<input type="hidden" name="` +
                  t.id +
                  `_s_` +
                  a.position +
                  `_` +
                  a.property +
                  `" value="` +
                  a.order +
                  `"/>`
              );
            });
            var a = t.getFilterSet();
            return (
              $.each(a, function (r, a) {
                $(e).append(
                  `<input type="hidden" name="` +
                    t.id +
                    `_f_` +
                    a.property +
                    `" value="` +
                    a.value +
                    `"/>`
                );
              }),
              !0
            );
          },
          createParameterString: function () {
            var e = this.limit,
              t = ``;
            ((t += e.id + `_p_=` + e.page),
              (t += `&` + e.id + `_mr_=` + e.maxRows));
            var r = e.getSortSet();
            $.each(r, function (r, a) {
              t +=
                `&` +
                e.id +
                `_s_` +
                a.position +
                `_` +
                a.property +
                `=` +
                a.order;
            });
            var a = e.getFilterSet();
            return (
              $.each(a, function (r, a) {
                t +=
                  `&` +
                  e.id +
                  `_f_` +
                  a.property +
                  `=` +
                  encodeURIComponent(a.value);
              }),
              e.exportType && (t += `&` + e.id + `_e_=` + e.exportType),
              (t += `&` + e.id + `_tr_=true`),
              this.worksheet.save && (t += `&` + e.id + `_sw_=true`),
              this.worksheet.filter && (t += `&` + e.id + `_fw_=true`),
              t
            );
          },
        }));
      var dynFilter = null,
        filterapi = {
          createDynFilter: function (e, t, r) {
            if (!dynFilter) {
              dynFilter = new classes.DynFilter(e, t, r);
              var a = $(e),
                o = a.width(),
                c = a.text();
              (a.width(o),
                a.parent().width(o),
                a.css(`overflow`, `visible`),
                a.html(
                  `<div id="dynFilterDiv"><input id="dynFilterInput" name="filter" style="width:` +
                    (o + 2) +
                    `px" value="" /></div>`
                ));
              var l = $(`#dynFilterInput`);
              (l.val(c),
                l.focus(),
                $(l).keypress(function (e) {
                  if (e.keyCode == 13) {
                    var t = l.val();
                    (a.text(``),
                      a.css(`overflow`, `hidden`),
                      a.text(t),
                      $.jmesa.addFilterToLimit(
                        dynFilter.id,
                        dynFilter.property,
                        t
                      ),
                      $.jmesa.onInvokeAction(dynFilter.id, `filter`),
                      (dynFilter = null));
                  }
                }),
                $(l).blur(function () {
                  var e = l.val();
                  (a.text(``),
                    a.css(`overflow`, `hidden`),
                    a.text(e),
                    $.jmesa.addFilterToLimit(
                      dynFilter.id,
                      dynFilter.property,
                      e
                    ),
                    (dynFilter = null));
                }));
            }
          },
          createDroplistDynFilter: function (e, t, r, a) {
            if (
              !dynFilter &&
              ((dynFilter = new classes.DynFilter(e, t, r)),
              !($(`#dynFilterDroplistDiv`).size() > 0))
            ) {
              var o = $(e),
                c = o.text(),
                l = o.width(),
                u = 1;
              ($.each(a, function () {
                if ((u++, u > 10)) return ((u = 10), !1);
              }),
                o.width(l),
                o.parent().width(l),
                o.html(`<div id="dynFilterDroplistDiv" style="top:17px">`));
              var f =
                `<select id="dynFilterDroplist" name="filter" size="` +
                u +
                `">`;
              ((f += `<option value=""> </option>`),
                $.each(a, function (e, t) {
                  e == c
                    ? (f +=
                        `<option selected="selected" value="` +
                        e +
                        `">` +
                        t +
                        `</option>`)
                    : (f += `<option value="` + e + `">` + t + `</option>`);
                }),
                (f += `</select>`));
              var p = $(`#dynFilterDroplistDiv`);
              p.html(f);
              var m = $(`#dynFilterDroplist`);
              (m.width() < l && m.width(l + 5), m.focus());
              var h = o.css(`backgroundColor`);
              (o.css({ backgroundColor: p.css(`backgroundColor`) }),
                $(m).change(function () {
                  var e = $(`#dynFilterDroplistDiv option:selected`).val();
                  (o.text(e),
                    $.jmesa.removeSpecifiedFilterFromLimit(t, r, `sed_`),
                    $.jmesa.addFilterToLimit(
                      dynFilter.id,
                      dynFilter.property,
                      e
                    ),
                    $.jmesa.onInvokeAction(dynFilter.id, `filter`),
                    (dynFilter = null));
                }),
                $(m).blur(function () {
                  var e = $(`#dynFilterDroplistDiv option:selected`).val();
                  (o.text(e),
                    $(`#dynFilterDroplistDiv`).remove(),
                    o.css({ backgroundColor: h }),
                    (dynFilter = null));
                }));
            }
          },
        },
        wsColumn = null,
        worksheetapi = {
          createWsColumn: function (e, t, r, a) {
            if (!wsColumn) {
              wsColumn = new classes.WsColumn(e, t, r, a);
              var o = $(e),
                c = o.width(),
                l = o.text();
              (o.width(c),
                o.parent().width(c),
                o.css(`overflow`, `visible`),
                o.html(
                  `<div id="wsColumnDiv"><input id="wsColumnInput" name="column" style="width:` +
                    (c + 3) +
                    `px" value=""/></div>`
                ));
              var u = $(`#wsColumnInput`);
              (u.val(l),
                u.focus(),
                $(`#wsColumnInput`).keypress(function (e) {
                  if (e.keyCode == 13) {
                    var t = u.val();
                    (o.text(``),
                      o.css(`overflow`, `hidden`),
                      o.text(t),
                      t != l && $.jmesa.submitWsColumn(l, t),
                      (wsColumn = null));
                  }
                }),
                $(`#wsColumnInput`).blur(function () {
                  var e = u.val();
                  (o.text(``),
                    o.css(`overflow`, `hidden`),
                    o.text(e),
                    e != l && $.jmesa.submitWsColumn(l, e),
                    (wsColumn = null));
                }));
            }
          },
          submitWsCheckboxColumn: function (e, t, r, a) {
            wsColumn = new classes.WsColumn(e, t, r, a);
            var o = e.checked,
              c = `unchecked`;
            o && (c = `checked`);
            var l = `unchecked`;
            (o || (l = `checked`),
              $.jmesa.submitWsColumn(l, c),
              (wsColumn = null));
          },
          submitWsColumn: function (originalValue, changedValue) {
            var data = `{ "id" : "` + wsColumn.id + `"`;
            data += `, "cp_" : "` + wsColumn.property + `"`;
            var props = wsColumn.uniqueProperties;
            ($.each(props, function (e, t) {
              data += `, "up_` + e + `" : "` + t + `"`;
            }),
              (data += `, "ov_" : "` + encodeURIComponent(originalValue) + `"`),
              (data += `, "cv_" : "` + encodeURIComponent(changedValue) + `"`),
              (data += `}`),
              $.post(`jmesa.wrk?`, eval(`(` + data + `)`), function (e) {}));
          },
        },
        effectsapi = {
          addDropShadow: function (e, t) {
            ((t ||= `jmesa`),
              $(`div.` + t + ` .table`)
                .wrap(
                  `<div class='wrap0'><div class='wrap1'><div class='wrap2'><div class='dropShadow'></div></div></div></div>`
                )
                .css({
                  background: `url(` + e + `shadow_back.gif) 100% repeat`,
                }),
              $(`.` + t + ` div.wrap0`).css({
                background: `url(` + e + `shadow.gif) right bottom no-repeat`,
                float: `left`,
              }),
              $(`.` + t + ` div.wrap1`).css({
                background: `url(` + e + `shadow180.gif) no-repeat`,
              }),
              $(`.` + t + ` div.wrap2`).css({
                background: `url(` + e + `corner_bl.gif) -18px 100% no-repeat`,
              }),
              $(`.` + t + ` div.dropShadow`).css({
                background: `url(` + e + `corner_tr.gif) 100% -18px no-repeat`,
              }),
              $(`div.` + t).append(`<div style="clear:both">&nbsp;</div>`));
          },
        };
      ($.extend(coreapi, filterapi),
        $.extend(coreapi, worksheetapi),
        $.extend(coreapi, effectsapi),
        ($.jmesa = {}),
        $.extend($.jmesa, coreapi));
    })(jQuery);
  }),
  require_jmesa = __commonJSMin(() => {
    var e = require_jquery_jmesa();
  }),
  require_calendar = __commonJSMin(() => {
    ((Calendar = function (e, t, r, a) {
      if (
        ((this.activeDiv = null),
        (this.currentDateEl = null),
        (this.getDateStatus = null),
        (this.getDateToolTip = null),
        (this.getDateText = null),
        (this.timeout = null),
        (this.onSelected = r || null),
        (this.onClose = a || null),
        (this.dragging = !1),
        (this.hidden = !1),
        (this.minYear = 1970),
        (this.maxYear = 2050),
        (this.dateFormat = Calendar._TT.DEF_DATE_FORMAT),
        (this.ttDateFormat = Calendar._TT.TT_DATE_FORMAT),
        (this.isPopup = !0),
        (this.weekNumbers = !0),
        (this.firstDayOfWeek = typeof e == `number` ? e : Calendar._FD),
        (this.showsOtherMonths = !1),
        (this.dateStr = t),
        (this.ar_days = null),
        (this.showsTime = !1),
        (this.time24 = !0),
        (this.yearStep = 2),
        (this.hiliteToday = !0),
        (this.multiple = null),
        (this.table = null),
        (this.element = null),
        (this.tbody = null),
        (this.firstdayname = null),
        (this.monthsCombo = null),
        (this.yearsCombo = null),
        (this.hilitedMonth = null),
        (this.activeMonth = null),
        (this.hilitedYear = null),
        (this.activeYear = null),
        (this.dateClicked = !1),
        Calendar._SDN === void 0)
      ) {
        Calendar._SDN_len === void 0 && (Calendar._SDN_len = 3);
        for (var o = [], c = 8; c > 0;)
          o[--c] = Calendar._DN[c].substr(0, Calendar._SDN_len);
        ((Calendar._SDN = o),
          Calendar._SMN_len === void 0 && (Calendar._SMN_len = 3),
          (o = []));
        for (var c = 12; c > 0;)
          o[--c] = Calendar._MN[c].substr(0, Calendar._SMN_len);
        Calendar._SMN = o;
      }
    }),
      (Calendar._C = null),
      (Calendar.is_ie =
        /msie/i.test(navigator.userAgent) &&
        !/opera/i.test(navigator.userAgent)),
      (Calendar.is_ie5 =
        Calendar.is_ie && /msie 5\.0/i.test(navigator.userAgent)),
      (Calendar.is_ie7 =
        Calendar.is_ie && /msie 7\.0/i.test(navigator.userAgent)),
      (Calendar.is_ie8 =
        Calendar.is_ie && /msie 8\.0/i.test(navigator.userAgent)),
      (Calendar.is_opera = /opera/i.test(navigator.userAgent)),
      (Calendar.is_khtml = /Konqueror|Safari|KHTML/i.test(navigator.userAgent)),
      (Calendar.getAbsolutePos = function (e) {
        var t = 0,
          r = 0,
          a = /^div$/i.test(e.tagName);
        (a && e.scrollLeft && (t = e.scrollLeft),
          a && e.scrollTop && (r = e.scrollTop));
        var o = { x: e.offsetLeft - t, y: e.offsetTop - r };
        if (e.offsetParent) {
          var c = this.getAbsolutePos(e.offsetParent);
          ((o.x += c.x), (o.y += c.y));
        }
        return o;
      }),
      (Calendar.isRelated = function (e, t) {
        var r = t.relatedTarget;
        if (!r) {
          var a = t.type;
          a == `mouseover`
            ? (r = t.fromElement)
            : a == `mouseout` && (r = t.toElement);
        }
        for (; r;) {
          if (r == e) return !0;
          r = r.parentNode;
        }
        return !1;
      }),
      (Calendar.removeClass = function (e, t) {
        if (e && e.className) {
          for (var r = e.className.split(` `), a = [], o = r.length; o > 0;)
            r[--o] != t && (a[a.length] = r[o]);
          e.className = a.join(` `);
        }
      }),
      (Calendar.addClass = function (e, t) {
        (Calendar.removeClass(e, t), (e.className += ` ` + t));
      }),
      (Calendar.getElement = function (e) {
        for (
          var t = Calendar.is_ie ? window.event.srcElement : e.currentTarget;
          t.nodeType != 1 || /^div$/i.test(t.tagName);
        )
          t = t.parentNode;
        return t;
      }),
      (Calendar.getTargetElement = function (e) {
        for (
          var t = Calendar.is_ie ? window.event.srcElement : e.target;
          t.nodeType != 1;
        )
          t = t.parentNode;
        return t;
      }),
      (Calendar.stopEvent = function (e) {
        return (
          (e ||= window.event),
          Calendar.is_ie
            ? ((e.cancelBubble = !0), (e.returnValue = !1))
            : (e.preventDefault(), e.stopPropagation()),
          !1
        );
      }),
      (Calendar.addEvent = function (e, t, r) {
        e.attachEvent
          ? e.attachEvent(`on` + t, r)
          : e.addEventListener
            ? e.addEventListener(t, r, !0)
            : (e[`on` + t] = r);
      }),
      (Calendar.removeEvent = function (e, t, r) {
        e.detachEvent
          ? e.detachEvent(`on` + t, r)
          : e.removeEventListener
            ? e.removeEventListener(t, r, !0)
            : (e[`on` + t] = null);
      }),
      (Calendar.createElement = function (e, t) {
        var r = null;
        return (
          (r = document.createElementNS
            ? document.createElementNS(`http://www.w3.org/1999/xhtml`, e)
            : document.createElement(e)),
          t !== void 0 && t.appendChild(r),
          r
        );
      }),
      (Calendar._add_evs = function (e) {
        (Calendar.addEvent(e, `mouseover`, Calendar.dayMouseOver),
          Calendar.addEvent(e, `mousedown`, Calendar.dayMouseDown),
          Calendar.addEvent(e, `mouseout`, Calendar.dayMouseOut),
          Calendar.is_ie &&
            (Calendar.addEvent(e, `dblclick`, Calendar.dayMouseDblClick),
            e.setAttribute(`unselectable`, !0)));
      }),
      (Calendar.findMonth = function (e) {
        return e.month === void 0
          ? e.parentNode.month === void 0
            ? null
            : e.parentNode
          : e;
      }),
      (Calendar.findYear = function (e) {
        return e.year === void 0
          ? e.parentNode.year === void 0
            ? null
            : e.parentNode
          : e;
      }),
      (Calendar.showMonthsCombo = function () {
        var e = Calendar._C;
        if (!e) return !1;
        var e = e,
          t = e.activeDiv,
          r = e.monthsCombo;
        (e.hilitedMonth && Calendar.removeClass(e.hilitedMonth, `hilite`),
          e.activeMonth && Calendar.removeClass(e.activeMonth, `active`));
        var a = e.monthsCombo.getElementsByTagName(`div`)[e.date.getMonth()];
        (Calendar.addClass(a, `active`), (e.activeMonth = a));
        var o = r.style;
        if (((o.display = `block`), t.navtype < 0))
          o.left = t.offsetLeft + `px`;
        else {
          var c = r.offsetWidth;
          (c === void 0 && (c = 50),
            (o.left = t.offsetLeft + t.offsetWidth - c + `px`));
        }
        o.top = t.offsetTop + t.offsetHeight + `px`;
      }),
      (Calendar.showYearsCombo = function (e) {
        var t = Calendar._C;
        if (!t) return !1;
        var t = t,
          r = t.activeDiv,
          a = t.yearsCombo;
        (t.hilitedYear && Calendar.removeClass(t.hilitedYear, `hilite`),
          t.activeYear && Calendar.removeClass(t.activeYear, `active`),
          (t.activeYear = null));
        for (
          var o = t.date.getFullYear() + (e ? 1 : -1),
            c = a.firstChild,
            l = !1,
            u = 12;
          u > 0;
          --u
        )
          (o >= t.minYear && o <= t.maxYear
            ? ((c.innerHTML = o),
              (c.year = o),
              (c.style.display = `block`),
              (l = !0))
            : (c.style.display = `none`),
            (c = c.nextSibling),
            (o += e ? t.yearStep : -t.yearStep));
        if (l) {
          var f = a.style;
          if (((f.display = `block`), r.navtype < 0))
            f.left = r.offsetLeft + `px`;
          else {
            var p = a.offsetWidth;
            (p === void 0 && (p = 50),
              (f.left = r.offsetLeft + r.offsetWidth - p + `px`));
          }
          f.top = r.offsetTop + r.offsetHeight + `px`;
        }
      }),
      (Calendar.tableMouseUp = function (e) {
        var t = Calendar._C;
        if (!t) return !1;
        t.timeout && clearTimeout(t.timeout);
        var r = t.activeDiv;
        if (!r) return !1;
        var a = Calendar.getTargetElement(e);
        ((e ||= window.event),
          Calendar.removeClass(r, `active`),
          (a == r || a.parentNode == r) && Calendar.cellClick(r, e));
        var o = Calendar.findMonth(a),
          c = null;
        if (o)
          ((c = new Date(t.date)),
            o.month != c.getMonth() &&
              (c.setMonth(o.month),
              t.setDate(c),
              (t.dateClicked = !1),
              t.callHandler()));
        else {
          var l = Calendar.findYear(a);
          l &&
            ((c = new Date(t.date)),
            l.year != c.getFullYear() &&
              (c.setFullYear(l.year),
              t.setDate(c),
              (t.dateClicked = !1),
              t.callHandler()));
        }
        return (
          Calendar.removeEvent(document, `mouseup`, Calendar.tableMouseUp),
          Calendar.removeEvent(document, `mouseover`, Calendar.tableMouseOver),
          Calendar.removeEvent(document, `mousemove`, Calendar.tableMouseOver),
          t._hideCombos(),
          (Calendar._C = null),
          Calendar.stopEvent(e)
        );
      }),
      (Calendar.tableMouseOver = function (e) {
        var t = Calendar._C;
        if (t) {
          var r = t.activeDiv,
            a = Calendar.getTargetElement(e);
          if (
            (a == r || a.parentNode == r
              ? (Calendar.addClass(r, `hilite active`),
                Calendar.addClass(r.parentNode, `rowhilite`))
              : ((r.navtype === void 0 ||
                  (r.navtype != 50 &&
                    (r.navtype == 0 || Math.abs(r.navtype) > 2))) &&
                  Calendar.removeClass(r, `active`),
                Calendar.removeClass(r, `hilite`),
                Calendar.removeClass(r.parentNode, `rowhilite`)),
            (e ||= window.event),
            r.navtype == 50 && a != r)
          ) {
            var o = Calendar.getAbsolutePos(r),
              c = r.offsetWidth,
              l = e.clientX,
              u,
              f = !0;
            (l > o.x + c ? ((u = l - o.x - c), (f = !1)) : (u = o.x - l),
              u < 0 && (u = 0));
            for (
              var p = r._range,
                m = r._current,
                h = Math.floor(u / 10) % p.length,
                g = p.length;
              --g >= 0 && p[g] != m;
            );
            for (; h-- > 0;)
              f ? --g < 0 && (g = p.length - 1) : ++g >= p.length && (g = 0);
            ((r.innerHTML = p[g]), t.onUpdateTime());
          }
          var v = Calendar.findMonth(a);
          if (v)
            v.month == t.date.getMonth()
              ? t.hilitedMonth && Calendar.removeClass(t.hilitedMonth, `hilite`)
              : (t.hilitedMonth &&
                  Calendar.removeClass(t.hilitedMonth, `hilite`),
                Calendar.addClass(v, `hilite`),
                (t.hilitedMonth = v));
          else {
            t.hilitedMonth && Calendar.removeClass(t.hilitedMonth, `hilite`);
            var y = Calendar.findYear(a);
            y
              ? y.year == t.date.getFullYear()
                ? t.hilitedYear && Calendar.removeClass(t.hilitedYear, `hilite`)
                : (t.hilitedYear &&
                    Calendar.removeClass(t.hilitedYear, `hilite`),
                  Calendar.addClass(y, `hilite`),
                  (t.hilitedYear = y))
              : t.hilitedYear && Calendar.removeClass(t.hilitedYear, `hilite`);
          }
          return Calendar.stopEvent(e);
        }
      }),
      (Calendar.tableMouseDown = function (e) {
        if (Calendar.getTargetElement(e) == Calendar.getElement(e))
          return Calendar.stopEvent(e);
      }),
      (Calendar.calDragIt = function (e) {
        var t = Calendar._C;
        if (!(t && t.dragging)) return !1;
        var r, a;
        (Calendar.is_ie
          ? ((a = window.event.clientY + document.body.scrollTop),
            (r = window.event.clientX + document.body.scrollLeft))
          : ((r = e.pageX), (a = e.pageY)),
          t.hideShowCovered());
        var o = t.element.style;
        return (
          (o.left = r - t.xOffs + `px`),
          (o.top = a - t.yOffs + `px`),
          Calendar.stopEvent(e)
        );
      }),
      (Calendar.calDragEnd = function (e) {
        var t = Calendar._C;
        if (!t) return !1;
        ((t.dragging = !1),
          Calendar.removeEvent(document, `mousemove`, Calendar.calDragIt),
          Calendar.removeEvent(document, `mouseup`, Calendar.calDragEnd),
          Calendar.tableMouseUp(e),
          t.hideShowCovered());
      }),
      (Calendar.dayMouseDown = function (e) {
        var t = Calendar.getElement(e);
        if (t.disabled) return !1;
        var r = t.calendar;
        return (
          (r.activeDiv = t),
          (Calendar._C = r),
          t.navtype == 300
            ? r.isPopup && r._dragStart(e)
            : (t.navtype == 50
                ? ((t._current = t.innerHTML),
                  Calendar.addEvent(
                    document,
                    `mousemove`,
                    Calendar.tableMouseOver
                  ))
                : Calendar.addEvent(
                    document,
                    Calendar.is_ie5 ? `mousemove` : `mouseover`,
                    Calendar.tableMouseOver
                  ),
              Calendar.addClass(t, `hilite active`),
              Calendar.addEvent(document, `mouseup`, Calendar.tableMouseUp)),
          t.navtype == -1 || t.navtype == 1
            ? (r.timeout && clearTimeout(r.timeout),
              (r.timeout = setTimeout(`Calendar.showMonthsCombo()`, 250)))
            : t.navtype == -2 || t.navtype == 2
              ? (r.timeout && clearTimeout(r.timeout),
                (r.timeout = setTimeout(
                  t.navtype > 0
                    ? `Calendar.showYearsCombo(true)`
                    : `Calendar.showYearsCombo(false)`,
                  250
                )))
              : (r.timeout = null),
          Calendar.stopEvent(e)
        );
      }),
      (Calendar.dayMouseDblClick = function (e) {
        (Calendar.cellClick(Calendar.getElement(e), e || window.event),
          Calendar.is_ie && document.selection.empty());
      }),
      (Calendar.dayMouseOver = function (e) {
        var t = Calendar.getElement(e);
        return Calendar.isRelated(t, e) || Calendar._C || t.disabled
          ? !1
          : (t.ttip &&
              (t.ttip.substr(0, 1) == `_` &&
                (t.ttip =
                  t.caldate.print(t.calendar.ttDateFormat) + t.ttip.substr(1)),
              (t.calendar.tooltips.innerHTML = t.ttip)),
            t.navtype != 300 &&
              (Calendar.addClass(t, `hilite`),
              t.caldate && Calendar.addClass(t.parentNode, `rowhilite`)),
            Calendar.stopEvent(e));
      }),
      (Calendar.dayMouseOut = function (e) {
        var t = Calendar.getElement(e);
        return Calendar.isRelated(t, e) || Calendar._C || t.disabled
          ? !1
          : (Calendar.removeClass(t, `hilite`),
            t.caldate && Calendar.removeClass(t.parentNode, `rowhilite`),
            t.calendar &&
              (t.calendar.tooltips.innerHTML = Calendar._TT.SEL_DATE),
            Calendar.stopEvent(e));
      }),
      (Calendar.cellClick = function (e, t) {
        var r = e.calendar,
          a = !1,
          o = !1,
          c = null;
        if (e.navtype === void 0) {
          (r.currentDateEl &&
            (Calendar.removeClass(r.currentDateEl, `selected`),
            Calendar.addClass(e, `selected`),
            (a = r.currentDateEl == e),
            a || (r.currentDateEl = e)),
            r.date.setDateOnly(e.caldate),
            (c = r.date));
          var l = !(r.dateClicked = !e.otherMonth);
          (!l && !r.currentDateEl
            ? r._toggleMultipleDate(new Date(c))
            : (o = !e.disabled),
            l && r._init(r.firstDayOfWeek, c));
        } else {
          if (e.navtype == 200) {
            (Calendar.removeClass(e, `hilite`), r.callCloseHandler());
            return;
          }
          ((c = new Date(r.date)),
            e.navtype == 0 && c.setDateOnly(new Date()),
            (r.dateClicked = !1));
          var u = c.getFullYear(),
            f = c.getMonth();
          function l(e) {
            var t = c.getDate(),
              r = c.getMonthDays(e);
            (t > r && c.setDate(r), c.setMonth(e));
          }
          switch (e.navtype) {
            case 400:
              Calendar.removeClass(e, `hilite`);
              var p = Calendar._TT.ABOUT;
              (p === void 0
                ? (p = `Help and about box text is not translated into this language.
If you know this language and you feel generous please update
the corresponding file in "lang" subdir to match calendar-en.js
and send it back to <mihai_bazon@yahoo.com> to get it into the distribution  ;-)

Thank you!
http://dynarch.com/mishoo/calendar.epl
`)
                : (p += r.showsTime ? Calendar._TT.ABOUT_TIME : ``),
                alert(p));
              return;
            case -2:
              u > r.minYear && c.setFullYear(u - 1);
              break;
            case -1:
              f > 0 ? l(f - 1) : u-- > r.minYear && (c.setFullYear(u), l(11));
              break;
            case 1:
              f < 11 ? l(f + 1) : u < r.maxYear && (c.setFullYear(u + 1), l(0));
              break;
            case 2:
              u < r.maxYear && c.setFullYear(u + 1);
              break;
            case 100:
              r.setFirstDayOfWeek(e.fdow);
              return;
            case 50:
              for (
                var m = e._range, h = e.innerHTML, g = m.length;
                --g >= 0 && m[g] != h;
              );
              (t && t.shiftKey
                ? --g < 0 && (g = m.length - 1)
                : ++g >= m.length && (g = 0),
                (e.innerHTML = m[g]),
                r.onUpdateTime());
              return;
            case 0:
              if (
                typeof r.getDateStatus == `function` &&
                r.getDateStatus(c, c.getFullYear(), c.getMonth(), c.getDate())
              )
                return !1;
              break;
          }
          c.equalsTo(r.date)
            ? e.navtype == 0 && (o = a = !0)
            : (r.setDate(c), (o = !0));
        }
        (o && t && r.callHandler(),
          a && (Calendar.removeClass(e, `hilite`), t && r.callCloseHandler()));
      }),
      (Calendar.prototype.create = function (e) {
        var t = null;
        (e
          ? ((t = e), (this.isPopup = !1))
          : ((t = document.getElementById(`dvForCalander`)
              ? document.getElementById(`dvForCalander`)
              : document.getElementsByTagName(`body`)[0]),
            (this.isPopup = !0)),
          (this.date = this.dateStr ? new Date(this.dateStr) : new Date()));
        var r = Calendar.createElement(`table`);
        ((this.table = r),
          (r.cellSpacing = 0),
          (r.cellPadding = 0),
          (r.calendar = this),
          Calendar.addEvent(r, `mousedown`, Calendar.tableMouseDown));
        var a = Calendar.createElement(`div`);
        ((this.element = a),
          (a.className = `calendar`),
          this.isPopup &&
            ((a.style.position = `absolute`), (a.style.display = `none`)),
          a.appendChild(r));
        var o = Calendar.createElement(`thead`, r),
          c = null,
          l = null,
          u = this,
          f = function (e, t, r) {
            return (
              (c = Calendar.createElement(`td`, l)),
              (c.colSpan = t),
              (c.className = `cbutton`),
              r != 0 && Math.abs(r) <= 2 && (c.className += ` nav`),
              Calendar._add_evs(c),
              (c.calendar = u),
              (c.navtype = r),
              (c.innerHTML = `<div unselectable='on'>` + e + `</div>`),
              c
            );
          };
        l = Calendar.createElement(`tr`, o);
        var p = 6;
        (this.isPopup && --p,
          this.weekNumbers && ++p,
          (f(`?`, 1, 400).ttip = Calendar._TT.INFO),
          (this.title = f(``, p, 300)),
          (this.title.className = `title`),
          this.isPopup &&
            ((this.title.ttip = Calendar._TT.DRAG_TO_MOVE),
            (this.title.style.cursor = `move`),
            (f(`&#x00d7;`, 1, 200).ttip = Calendar._TT.CLOSE)),
          (l = Calendar.createElement(`tr`, o)),
          (l.className = `headrow`),
          (this._nav_py = f(`&#x00ab;`, 1, -2)),
          (this._nav_py.ttip = Calendar._TT.PREV_YEAR),
          (this._nav_pm = f(`&#x2039;`, 1, -1)),
          (this._nav_pm.ttip = Calendar._TT.PREV_MONTH),
          (this._nav_now = f(Calendar._TT.TODAY, this.weekNumbers ? 4 : 3, 0)),
          (this._nav_now.ttip = Calendar._TT.GO_TODAY),
          (this._nav_nm = f(`&#x203a;`, 1, 1)),
          (this._nav_nm.ttip = Calendar._TT.NEXT_MONTH),
          (this._nav_ny = f(`&#x00bb;`, 1, 2)),
          (this._nav_ny.ttip = Calendar._TT.NEXT_YEAR),
          (l = Calendar.createElement(`tr`, o)),
          (l.className = `daynames`),
          this.weekNumbers &&
            ((c = Calendar.createElement(`td`, l)),
            (c.className = `name wn`),
            (c.innerHTML = Calendar._TT.WK)));
        for (var m = 7; m > 0; --m)
          ((c = Calendar.createElement(`td`, l)),
            m ||
              ((c.navtype = 100), (c.calendar = this), Calendar._add_evs(c)));
        ((this.firstdayname = this.weekNumbers
          ? l.firstChild.nextSibling
          : l.firstChild),
          this._displayWeekdays());
        var h = Calendar.createElement(`tbody`, r);
        for (this.tbody = h, m = 6; m > 0; --m) {
          ((l = Calendar.createElement(`tr`, h)),
            this.weekNumbers && (c = Calendar.createElement(`td`, l)));
          for (var g = 7; g > 0; --g)
            ((c = Calendar.createElement(`td`, l)),
              (c.calendar = this),
              Calendar._add_evs(c));
        }
        this.showsTime
          ? ((l = Calendar.createElement(`tr`, h)),
            (l.className = `time`),
            (c = Calendar.createElement(`td`, l)),
            (c.className = `time`),
            (c.colSpan = 2),
            (c.innerHTML = Calendar._TT.TIME || `&nbsp;`),
            (c = Calendar.createElement(`td`, l)),
            (c.className = `time`),
            (c.colSpan = this.weekNumbers ? 4 : 3),
            (function () {
              function e(e, t, r, a) {
                var o = Calendar.createElement(`span`, c);
                if (
                  ((o.className = e),
                  (o.innerHTML = t),
                  (o.calendar = u),
                  (o.ttip = Calendar._TT.TIME_PART),
                  (o.navtype = 50),
                  (o._range = []),
                  typeof r != `number`)
                )
                  o._range = r;
                else
                  for (var l = r; l <= a; ++l) {
                    var f = l < 10 && a >= 10 ? `0` + l : `` + l;
                    o._range[o._range.length] = f;
                  }
                return (Calendar._add_evs(o), o);
              }
              var t = u.date.getHours(),
                r = u.date.getMinutes(),
                a = !u.time24,
                o = t > 12;
              a && o && (t -= 12);
              var f = e(`hour`, t, +!!a, a ? 12 : 23),
                p = Calendar.createElement(`span`, c);
              ((p.innerHTML = `:`), (p.className = `colon`));
              var m = e(`minute`, r, 0, 59),
                h = null;
              ((c = Calendar.createElement(`td`, l)),
                (c.className = `time`),
                (c.colSpan = 2),
                a
                  ? (h = e(`ampm`, o ? `pm` : `am`, [`am`, `pm`]))
                  : (c.innerHTML = `&nbsp;`),
                (u.onSetTime = function () {
                  var e,
                    t = this.date.getHours(),
                    r = this.date.getMinutes();
                  (a &&
                    ((e = t >= 12),
                    e && (t -= 12),
                    t == 0 && (t = 12),
                    (h.innerHTML = e ? `pm` : `am`)),
                    (f.innerHTML = t < 10 ? `0` + t : t),
                    (m.innerHTML = r < 10 ? `0` + r : r));
                }),
                (u.onUpdateTime = function () {
                  var e = this.date,
                    t = parseInt(f.innerHTML, 10);
                  a &&
                    (/pm/i.test(h.innerHTML) && t < 12
                      ? (t += 12)
                      : /am/i.test(h.innerHTML) && t == 12 && (t = 0));
                  var r = e.getDate(),
                    o = e.getMonth(),
                    c = e.getFullYear();
                  (e.setHours(t),
                    e.setMinutes(parseInt(m.innerHTML, 10)),
                    e.setFullYear(c),
                    e.setMonth(o),
                    e.setDate(r),
                    (this.dateClicked = !1),
                    this.callHandler());
                }));
            })())
          : (this.onSetTime = this.onUpdateTime = function () {});
        var v = Calendar.createElement(`tfoot`, r);
        for (
          l = Calendar.createElement(`tr`, v),
            l.className = `footrow`,
            c = f(Calendar._TT.SEL_DATE, this.weekNumbers ? 8 : 7, 300),
            c.className = `ttip`,
            this.isPopup &&
              ((c.ttip = Calendar._TT.DRAG_TO_MOVE), (c.style.cursor = `move`)),
            this.tooltips = c,
            a = Calendar.createElement(`div`, this.element),
            this.monthsCombo = a,
            a.className = `combo`,
            m = 0;
          m < Calendar._MN.length;
          ++m
        ) {
          var y = Calendar.createElement(`div`);
          ((y.className = Calendar.is_ie ? `label-IEfix` : `label`),
            (y.month = m),
            (y.innerHTML = Calendar._SMN[m]),
            a.appendChild(y));
        }
        for (
          a = Calendar.createElement(`div`, this.element),
            this.yearsCombo = a,
            a.className = `combo`,
            m = 12;
          m > 0;
          --m
        ) {
          var b = Calendar.createElement(`div`);
          ((b.className = Calendar.is_ie ? `label-IEfix` : `label`),
            a.appendChild(b));
        }
        (this._init(this.firstDayOfWeek, this.date),
          t.appendChild(this.element));
      }),
      (Calendar._keyEvent = function (e) {
        var t = window._dynarch_popupCalendar;
        if (!t || t.multiple) return !1;
        Calendar.is_ie && (e = window.event);
        var r = Calendar.is_ie || e.type == `keypress`,
          a = e.keyCode;
        if (e.ctrlKey)
          switch (a) {
            case 37:
              r && Calendar.cellClick(t._nav_pm);
              break;
            case 38:
              r && Calendar.cellClick(t._nav_py);
              break;
            case 39:
              r && Calendar.cellClick(t._nav_nm);
              break;
            case 40:
              r && Calendar.cellClick(t._nav_ny);
              break;
            default:
              return !1;
          }
        else
          switch (a) {
            case 32:
              Calendar.cellClick(t._nav_now);
              break;
            case 27:
              r && t.callCloseHandler();
              break;
            case 37:
            case 38:
            case 39:
            case 40:
              if (r) {
                var o = a == 37 || a == 38,
                  c,
                  l,
                  u,
                  f,
                  p = a == 37 || a == 39 ? 1 : 7;
                function e() {
                  f = t.currentDateEl;
                  var e = f.pos;
                  ((c = e & 15), (l = e >> 4), (u = t.ar_days[l][c]));
                }
                e();
                function r() {
                  var e = new Date(t.date);
                  (e.setDate(e.getDate() - p), t.setDate(e));
                }
                function m() {
                  var e = new Date(t.date);
                  (e.setDate(e.getDate() + p), t.setDate(e));
                }
                for (;;) {
                  switch (a) {
                    case 37:
                      if (--c >= 0) u = t.ar_days[l][c];
                      else {
                        ((c = 6), (a = 38));
                        continue;
                      }
                      break;
                    case 38:
                      --l >= 0 ? (u = t.ar_days[l][c]) : (r(), e());
                      break;
                    case 39:
                      if (++c < 7) u = t.ar_days[l][c];
                      else {
                        ((c = 0), (a = 40));
                        continue;
                      }
                      break;
                    case 40:
                      ++l < t.ar_days.length
                        ? (u = t.ar_days[l][c])
                        : (m(), e());
                      break;
                  }
                  break;
                }
                u && (u.disabled ? (o ? r() : m()) : Calendar.cellClick(u));
              }
              break;
            case 13:
              r && Calendar.cellClick(t.currentDateEl, e);
              break;
            default:
              return !1;
          }
        return Calendar.stopEvent(e);
      }),
      (Calendar.prototype._init = function (e, t) {
        var r = new Date(),
          a = r.getFullYear(),
          o = r.getMonth(),
          c = r.getDate();
        this.table.style.visibility = `hidden`;
        var l = t.getFullYear();
        (l < this.minYear
          ? ((l = this.minYear), t.setFullYear(l))
          : l > this.maxYear && ((l = this.maxYear), t.setFullYear(l)),
          (this.firstDayOfWeek = e),
          (this.date = new Date(t)));
        var u = t.getMonth(),
          f = t.getDate();
        (t.getMonthDays(), t.setDate(1));
        var p = (t.getDay() - this.firstDayOfWeek) % 7;
        (p < 0 && (p += 7), t.setDate(-p), t.setDate(t.getDate() + 1));
        var m = this.tbody.firstChild;
        Calendar._SMN[u];
        for (
          var h = (this.ar_days = []),
            g = Calendar._TT.WEEKEND,
            v = this.multiple ? (this.datesCells = {}) : null,
            y = 0;
          y < 6;
          ++y, m = m.nextSibling
        ) {
          var b = m.firstChild;
          (this.weekNumbers &&
            ((b.className = `day wn`),
            (b.innerHTML = t.getWeekNumber()),
            (b = b.nextSibling)),
            (m.className = `daysrow`));
          for (
            var x = !1, S, C = (h[y] = []), w = 0;
            w < 7;
            ++w, b = b.nextSibling, t.setDate(S + 1)
          ) {
            S = t.getDate();
            var T = t.getDay();
            ((b.className = `day`), (b.pos = (y << 4) | w), (C[w] = b));
            var E = t.getMonth() == u;
            if (E) ((b.otherMonth = !1), (x = !0));
            else if (this.showsOtherMonths)
              ((b.className += ` othermonth`), (b.otherMonth = !0));
            else {
              ((b.className = `emptycell`),
                (b.innerHTML = `&nbsp;`),
                (b.disabled = !0));
              continue;
            }
            if (
              ((b.disabled = !1),
              (b.innerHTML = this.getDateText ? this.getDateText(t, S) : S),
              v && (v[t.print(`%Y%m%d`)] = b),
              this.getDateStatus)
            ) {
              var D = this.getDateStatus(t, l, u, S);
              if (this.getDateToolTip) {
                var O = this.getDateToolTip(t, l, u, S);
                O && (b.title = O);
              }
              D === !0
                ? ((b.className += ` disabled`), (b.disabled = !0))
                : (/disabled/i.test(D) && (b.disabled = !0),
                  (b.className += ` ` + D));
            }
            b.disabled ||
              ((b.caldate = new Date(t)),
              (b.ttip = `_`),
              !this.multiple &&
                E &&
                S == f &&
                this.hiliteToday &&
                ((b.className += ` selected`), (this.currentDateEl = b)),
              t.getFullYear() == a &&
                t.getMonth() == o &&
                S == c &&
                ((b.className += ` today`),
                (b.ttip += Calendar._TT.PART_TODAY)),
              g.indexOf(T.toString()) != -1 &&
                (b.className += b.otherMonth ? ` oweekend` : ` weekend`));
          }
          x || this.showsOtherMonths || (m.className = `emptyrow`);
        }
        ((this.title.innerHTML = Calendar._MN[u] + `, ` + l),
          this.onSetTime(),
          (this.table.style.visibility = `visible`),
          this._initMultipleDates());
      }),
      (Calendar.prototype._initMultipleDates = function () {
        if (this.multiple)
          for (var e in this.multiple) {
            var t = this.datesCells[e];
            this.multiple[e] && t && (t.className += ` selected`);
          }
      }),
      (Calendar.prototype._toggleMultipleDate = function (e) {
        if (this.multiple) {
          var t = e.print(`%Y%m%d`),
            r = this.datesCells[t];
          r &&
            (this.multiple[t]
              ? (Calendar.removeClass(r, `selected`), delete this.multiple[t])
              : (Calendar.addClass(r, `selected`), (this.multiple[t] = e)));
        }
      }),
      (Calendar.prototype.setDateToolTipHandler = function (e) {
        this.getDateToolTip = e;
      }),
      (Calendar.prototype.setDate = function (e) {
        e.equalsTo(this.date) || this._init(this.firstDayOfWeek, e);
      }),
      (Calendar.prototype.refresh = function () {
        this._init(this.firstDayOfWeek, this.date);
      }),
      (Calendar.prototype.setFirstDayOfWeek = function (e) {
        (this._init(e, this.date), this._displayWeekdays());
      }),
      (Calendar.prototype.setDateStatusHandler =
        Calendar.prototype.setDisabledHandler =
          function (e) {
            this.getDateStatus = e;
          }),
      (Calendar.prototype.setRange = function (e, t) {
        ((this.minYear = e), (this.maxYear = t));
      }),
      (Calendar.prototype.callHandler = function () {
        this.onSelected &&
          this.onSelected(this, this.date.print(this.dateFormat));
      }),
      (Calendar.prototype.callCloseHandler = function () {
        (this.onClose && this.onClose(this), this.hideShowCovered());
      }),
      (Calendar.prototype.destroy = function () {
        (this.element.parentNode.removeChild(this.element),
          (Calendar._C = null),
          (window._dynarch_popupCalendar = null));
      }),
      (Calendar.prototype.reparent = function (e) {
        var t = this.element;
        (t.parentNode.removeChild(t), e.appendChild(t));
      }),
      (Calendar._checkCalendar = function (e) {
        var t = window._dynarch_popupCalendar;
        if (!t) return !1;
        for (
          var r = Calendar.is_ie
            ? Calendar.getElement(e)
            : Calendar.getTargetElement(e);
          r != null && r != t.element;
          r = r.parentNode
        );
        if (r == null)
          return (
            window._dynarch_popupCalendar.callCloseHandler(),
            Calendar.stopEvent(e)
          );
      }),
      (Calendar.prototype.show = function () {
        for (
          var e = this.table.getElementsByTagName(`tr`), t = e.length;
          t > 0;
        ) {
          var r = e[--t];
          Calendar.removeClass(r, `rowhilite`);
          for (var a = r.getElementsByTagName(`td`), o = a.length; o > 0;) {
            var c = a[--o];
            (Calendar.removeClass(c, `hilite`),
              Calendar.removeClass(c, `active`));
          }
        }
        ((this.element.style.display = `block`),
          (this.hidden = !1),
          this.isPopup &&
            ((window._dynarch_popupCalendar = this),
            Calendar.addEvent(document, `keydown`, Calendar._keyEvent),
            Calendar.addEvent(document, `keypress`, Calendar._keyEvent),
            Calendar.addEvent(document, `mousedown`, Calendar._checkCalendar)),
          this.hideShowCovered());
      }),
      (Calendar.prototype.hide = function () {
        (this.isPopup &&
          (Calendar.removeEvent(document, `keydown`, Calendar._keyEvent),
          Calendar.removeEvent(document, `keypress`, Calendar._keyEvent),
          Calendar.removeEvent(document, `mousedown`, Calendar._checkCalendar)),
          (this.element.style.display = `none`),
          (this.hidden = !0),
          this.hideShowCovered());
      }),
      (Calendar.prototype.showAt = function (e, t) {
        var r = this.element.style;
        ((r.left = e + `px`), (r.top = t + `px`), this.show());
      }),
      (Calendar.prototype.showAtElement = function (e, t, r, a) {
        var o = this,
          c = Calendar.getAbsolutePos(e);
        if ((r && a && ((c.x -= r), (c.y -= a)), !t || typeof t != `string`))
          return (this.showAt(c.x, c.y + e.offsetHeight), !0);
        function l(e) {
          (e.x < 0 && (e.x = 0), e.y < 0 && (e.y = 0));
          var t = document.createElement(`div`),
            r = t.style;
          ((r.position = `absolute`),
            (r.right = r.bottom = r.width = r.height = `0px`),
            document.body.appendChild(t));
          var a = Calendar.getAbsolutePos(t);
          (document.body.removeChild(t),
            Calendar.is_ie && !(Calendar.is_ie7 || Calendar.is_ie8)
              ? ((a.y += document.body.scrollTop),
                (a.x += document.body.scrollLeft))
              : ((a.y += window.scrollY), (a.x += window.scrollX)));
          var o = e.x + e.width - a.x;
          (o > 0 && (e.x -= o),
            (o = e.y + e.height - a.y),
            o > 0 && (e.y -= o));
        }
        ((this.element.style.display = `block`),
          (Calendar.continuation_for_the_fucking_khtml_browser = function () {
            var r = o.element.offsetWidth,
              a = o.element.offsetHeight;
            o.element.style.display = `none`;
            var u = t.substr(0, 1),
              f = `l`;
            switch ((t.length > 1 && (f = t.substr(1, 1)), u)) {
              case `T`:
                c.y -= a;
                break;
              case `B`:
                c.y += e.offsetHeight;
                break;
              case `C`:
                c.y += (e.offsetHeight - a) / 2;
                break;
              case `t`:
                c.y += e.offsetHeight - a;
                break;
              case `b`:
                break;
            }
            switch (f) {
              case `L`:
                c.x -= r;
                break;
              case `R`:
                c.x += e.offsetWidth;
                break;
              case `C`:
                c.x += (e.offsetWidth - r) / 2;
                break;
              case `l`:
                c.x += e.offsetWidth - r;
                break;
              case `r`:
                break;
            }
            ((c.width = r),
              (c.height = a + 40),
              (o.monthsCombo.style.display = `none`),
              l(c),
              o.showAt(c.x, c.y));
          }),
          Calendar.is_khtml
            ? setTimeout(
                `Calendar.continuation_for_the_fucking_khtml_browser()`,
                10
              )
            : Calendar.continuation_for_the_fucking_khtml_browser());
      }),
      (Calendar.prototype.setDateFormat = function (e) {
        this.dateFormat = e;
      }),
      (Calendar.prototype.setTtDateFormat = function (e) {
        this.ttDateFormat = e;
      }),
      (Calendar.prototype.parseDate = function (e, t) {
        ((t ||= this.dateFormat), this.setDate(Date.parseDate(e, t)));
      }),
      (Calendar.prototype.hideShowCovered = function () {
        if (!Calendar.is_ie && !Calendar.is_opera) return;
        function e(e) {
          var t = e.style.visibility;
          return (
            (t ||=
              document.defaultView &&
              typeof document.defaultView.getComputedStyle == `function`
                ? Calendar.is_khtml
                  ? ``
                  : document.defaultView
                      .getComputedStyle(e, ``)
                      .getPropertyValue(`visibility`)
                : e.currentStyle
                  ? e.currentStyle.visibility
                  : ``),
            t
          );
        }
        for (
          var t = [`applet`, `iframe`, `select`],
            r = this.element,
            a = Calendar.getAbsolutePos(r),
            o = a.x,
            c = r.offsetWidth + o,
            l = a.y,
            u = r.offsetHeight + l,
            f = t.length;
          f > 0;
        )
          for (
            var p = document.getElementsByTagName(t[--f]),
              m = null,
              h = p.length;
            h > 0;
          ) {
            ((m = p[--h]), (a = Calendar.getAbsolutePos(m)));
            var g = a.x,
              v = m.offsetWidth + g,
              y = a.y,
              b = m.offsetHeight + y;
            this.hidden || g > c || v < o || y > u || b < l
              ? ((m.__msh_save_visibility ||= e(m)),
                (m.style.visibility = m.__msh_save_visibility))
              : ((m.__msh_save_visibility ||= e(m)),
                (m.style.visibility = `hidden`));
          }
      }),
      (Calendar.prototype._displayWeekdays = function () {
        for (
          var e = this.firstDayOfWeek,
            t = this.firstdayname,
            r = Calendar._TT.WEEKEND,
            a = 0;
          a < 7;
          ++a
        ) {
          t.className = `day name`;
          var o = (a + e) % 7;
          (a &&
            ((t.ttip = Calendar._TT.DAY_FIRST.replace(`%s`, Calendar._DN[o])),
            (t.navtype = 100),
            (t.calendar = this),
            (t.fdow = o),
            Calendar._add_evs(t)),
            r.indexOf(o.toString()) != -1 && Calendar.addClass(t, `weekend`),
            (t.innerHTML = Calendar._SDN[(a + e) % 7]),
            (t = t.nextSibling));
        }
      }),
      (Calendar.prototype._hideCombos = function () {
        ((this.monthsCombo.style.display = `none`),
          (this.yearsCombo.style.display = `none`));
      }),
      (Calendar.prototype._dragStart = function (e) {
        if (!this.dragging) {
          this.dragging = !0;
          var t, r;
          Calendar.is_ie
            ? ((r = window.event.clientY + document.body.scrollTop),
              (t = window.event.clientX + document.body.scrollLeft))
            : ((r = e.clientY + window.scrollY),
              (t = e.clientX + window.scrollX));
          var a = this.element.style;
          ((this.xOffs = t - parseInt(a.left)),
            (this.yOffs = r - parseInt(a.top)),
            Calendar.addEvent(document, `mousemove`, Calendar.calDragIt),
            Calendar.addEvent(document, `mouseup`, Calendar.calDragEnd));
        }
      }),
      (Date._MD = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]),
      (Date.SECOND = 1e3),
      (Date.MINUTE = 60 * Date.SECOND),
      (Date.HOUR = 60 * Date.MINUTE),
      (Date.DAY = 24 * Date.HOUR),
      (Date.WEEK = 7 * Date.DAY),
      (Date.parseDate = function (e, t) {
        var r = new Date(),
          a = 0,
          o = -1,
          c = 0,
          l = e.split(/\W+/),
          u = t.match(/%./g),
          f = 0,
          p = 0,
          m = 0,
          h = 0;
        for (f = 0; f < l.length; ++f)
          if (l[f])
            switch (u[f]) {
              case `%d`:
              case `%e`:
                c = parseInt(l[f], 10);
                break;
              case `%m`:
                o = parseInt(l[f], 10) - 1;
                break;
              case `%Y`:
              case `%y`:
                ((a = parseInt(l[f], 10)),
                  a < 100 && (a += a > 29 ? 1900 : 2e3));
                break;
              case `%b`:
              case `%B`:
                for (p = 0; p < 12; ++p)
                  if (
                    Calendar._MN[p].substr(0, l[f].length).toLowerCase() ==
                    l[f].toLowerCase()
                  ) {
                    o = p;
                    break;
                  }
                break;
              case `%H`:
              case `%I`:
              case `%k`:
              case `%l`:
                m = parseInt(l[f], 10);
                break;
              case `%P`:
              case `%p`:
                /pm/i.test(l[f]) && m < 12
                  ? (m += 12)
                  : /am/i.test(l[f]) && m >= 12 && (m -= 12);
                break;
              case `%M`:
                h = parseInt(l[f], 10);
                break;
            }
        if (
          (isNaN(a) && (a = r.getFullYear()),
          isNaN(o) && (o = r.getMonth()),
          isNaN(c) && (c = r.getDate()),
          isNaN(m) && (m = r.getHours()),
          isNaN(h) && (h = r.getMinutes()),
          a != 0 && o != -1 && c != 0)
        )
          return new Date(a, o, c, m, h, 0);
        for (a = 0, o = -1, c = 0, f = 0; f < l.length; ++f)
          if (l[f].search(/[a-zA-Z]+/) != -1) {
            var g = -1;
            for (p = 0; p < 12; ++p)
              if (
                Calendar._MN[p].substr(0, l[f].length).toLowerCase() ==
                l[f].toLowerCase()
              ) {
                g = p;
                break;
              }
            g != -1 && (o != -1 && (c = o + 1), (o = g));
          } else
            parseInt(l[f], 10) <= 12 && o == -1
              ? (o = l[f] - 1)
              : parseInt(l[f], 10) > 31 && a == 0
                ? ((a = parseInt(l[f], 10)),
                  a < 100 && (a += a > 29 ? 1900 : 2e3))
                : c == 0 && (c = l[f]);
        return (
          a == 0 && (a = r.getFullYear()),
          o != -1 && c != 0 ? new Date(a, o, c, m, h, 0) : r
        );
      }),
      (Date.prototype.getMonthDays = function (e) {
        var t = this.getFullYear();
        return (
          e === void 0 && (e = this.getMonth()),
          t % 4 == 0 && (t % 100 != 0 || t % 400 == 0) && e == 1
            ? 29
            : Date._MD[e]
        );
      }),
      (Date.prototype.getDayOfYear = function () {
        var e =
          new Date(
            this.getFullYear(),
            this.getMonth(),
            this.getDate(),
            0,
            0,
            0
          ) - new Date(this.getFullYear(), 0, 0, 0, 0, 0);
        return Math.floor(e / Date.DAY);
      }),
      (Date.prototype.getWeekNumber = function () {
        var e = new Date(
            this.getFullYear(),
            this.getMonth(),
            this.getDate(),
            0,
            0,
            0
          ),
          t = e.getDay();
        e.setDate(e.getDate() - ((t + 6) % 7) + 3);
        var r = e.valueOf();
        return (
          e.setMonth(0),
          e.setDate(4),
          Math.round((r - e.valueOf()) / (7 * 864e5)) + 1
        );
      }),
      (Date.prototype.equalsTo = function (e) {
        return (
          this.getFullYear() == e.getFullYear() &&
          this.getMonth() == e.getMonth() &&
          this.getDate() == e.getDate() &&
          this.getHours() == e.getHours() &&
          this.getMinutes() == e.getMinutes()
        );
      }),
      (Date.prototype.setDateOnly = function (e) {
        var t = new Date(e);
        (this.setDate(1),
          this.setFullYear(t.getFullYear()),
          this.setMonth(t.getMonth()),
          this.setDate(t.getDate()));
      }),
      (Date.prototype.print = function (e) {
        var t = this.getMonth(),
          r = this.getDate(),
          a = this.getFullYear(),
          o = this.getWeekNumber(),
          c = this.getDay(),
          l = {},
          u = this.getHours(),
          f = u >= 12,
          p = f ? u - 12 : u,
          m = this.getDayOfYear();
        p == 0 && (p = 12);
        var h = this.getMinutes(),
          g = this.getSeconds();
        ((l[`%a`] = Calendar._SDN[c]),
          (l[`%A`] = Calendar._DN[c]),
          (l[`%b`] = Calendar._SMN[t]),
          (l[`%B`] = Calendar._MN[t]),
          (l[`%C`] = 1 + Math.floor(a / 100)),
          (l[`%d`] = r < 10 ? `0` + r : r),
          (l[`%e`] = r),
          (l[`%H`] = u < 10 ? `0` + u : u),
          (l[`%I`] = p < 10 ? `0` + p : p),
          (l[`%j`] = m < 100 ? (m < 10 ? `00` + m : `0` + m) : m),
          (l[`%k`] = u),
          (l[`%l`] = p),
          (l[`%m`] = t < 9 ? `0` + (1 + t) : 1 + t),
          (l[`%M`] = h < 10 ? `0` + h : h),
          (l[`%n`] = `
`),
          (l[`%o`] = t + 1),
          (l[`%p`] = f ? `PM` : `AM`),
          (l[`%P`] = f ? `pm` : `am`),
          (l[`%s`] = Math.floor(this.getTime() / 1e3)),
          (l[`%S`] = g < 10 ? `0` + g : g),
          (l[`%t`] = `	`),
          (l[`%U`] = l[`%W`] = l[`%V`] = o < 10 ? `0` + o : o),
          (l[`%u`] = c + 1),
          (l[`%w`] = c),
          (l[`%y`] = (`` + a).substr(2, 2)),
          (l[`%Y`] = a),
          (l[`%%`] = `%`));
        var v = /%./g;
        if (!Calendar.is_ie5 && !Calendar.is_khtml)
          return e.replace(v, function (e) {
            return l[e] || e;
          });
        for (var y = e.match(v), b = 0; b < y.length; b++) {
          var x = l[y[b]];
          x && ((v = new RegExp(y[b], `g`)), (e = e.replace(v, x)));
        }
        return e;
      }),
      (Date.prototype.__msh_oldSetFullYear = Date.prototype.setFullYear),
      (Date.prototype.setFullYear = function (e) {
        var t = new Date(this);
        (t.__msh_oldSetFullYear(e),
          t.getMonth() != this.getMonth() && this.setDate(28),
          this.__msh_oldSetFullYear(e));
      }),
      (window._dynarch_popupCalendar = null));
  }),
  require_calendar_en = __commonJSMin(() => {
    ((Calendar._DN = [
      `Sunday`,
      `Monday`,
      `Tuesday`,
      `Wednesday`,
      `Thursday`,
      `Friday`,
      `Saturday`,
      `Sunday`,
    ]),
      (Calendar._SDN = [
        `Sun`,
        `Mon`,
        `Tue`,
        `Wed`,
        `Thu`,
        `Fri`,
        `Sat`,
        `Sun`,
      ]),
      (Calendar._FD = 0),
      (Calendar._MN = [
        `January`,
        `February`,
        `March`,
        `April`,
        `May`,
        `June`,
        `July`,
        `August`,
        `September`,
        `October`,
        `November`,
        `December`,
      ]),
      (Calendar._SMN = [
        `Jan`,
        `Feb`,
        `Mar`,
        `Apr`,
        `May`,
        `Jun`,
        `Jul`,
        `Aug`,
        `Sep`,
        `Oct`,
        `Nov`,
        `Dec`,
      ]),
      (Calendar._TT = {}),
      (Calendar._TT.INFO = `About the calendar`),
      (Calendar._TT.ABOUT = `DHTML Date/Time Selector
(c) dynarch.com 2002-2005 / Author: Mihai Bazon
For latest version visit: http://www.dynarch.com/projects/calendar/
Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details.

Date selection:
- Use the «, » buttons to select year
- Use the ‹, › buttons to select month
- Hold mouse button on any of the above buttons for faster selection.`),
      (Calendar._TT.ABOUT_TIME = `

Time selection:
- Click on any of the time parts to increase it
- or Shift-click to decrease it
- or click and drag for faster selection.`),
      (Calendar._TT.PREV_YEAR = `Prev. year (hold for menu)`),
      (Calendar._TT.PREV_MONTH = `Prev. month (hold for menu)`),
      (Calendar._TT.GO_TODAY = `Go Today`),
      (Calendar._TT.NEXT_MONTH = `Next month (hold for menu)`),
      (Calendar._TT.NEXT_YEAR = `Next year (hold for menu)`),
      (Calendar._TT.SEL_DATE = `Select date`),
      (Calendar._TT.DRAG_TO_MOVE = `Drag to move`),
      (Calendar._TT.PART_TODAY = ` (today)`),
      (Calendar._TT.DAY_FIRST = `Display %s first`),
      (Calendar._TT.WEEKEND = `0,6`),
      (Calendar._TT.CLOSE = `Close`),
      (Calendar._TT.TODAY = `Today`),
      (Calendar._TT.TIME_PART = `(Shift-)Click or drag to change value`),
      (Calendar._TT.DEF_DATE_FORMAT = `%Y-%m-%d`),
      (Calendar._TT.TT_DATE_FORMAT = `%a, %b %e`),
      (Calendar._TT.WK = `wk`),
      (Calendar._TT.TIME = `Time:`));
  }),
  require_calendar_setup = __commonJSMin(() => {
    Calendar.setup = function (e) {
      function t(t, r) {
        e[t] === void 0 && (e[t] = r);
      }
      (t(`inputField`, null),
        t(`displayArea`, null),
        t(`cbutton`, null),
        t(`eventName`, `click`),
        t(`ifFormat`, `%Y/%m/%d`),
        t(`daFormat`, `%Y/%m/%d`),
        t(`singleClick`, !0),
        t(`disableFunc`, null),
        t(`dateStatusFunc`, e.disableFunc),
        t(`dateText`, null),
        t(`firstDay`, null),
        t(`align`, `Br`),
        t(`range`, [1900, 2999]),
        t(`weekNumbers`, !0),
        t(`flat`, null),
        t(`flatCallback`, null),
        t(`onSelect`, null),
        t(`onClose`, null),
        t(`onUpdate`, null),
        t(`date`, null),
        t(`showsTime`, !1),
        t(`timeFormat`, `24`),
        t(`electric`, !0),
        t(`step`, 1),
        t(`position`, null),
        t(`cache`, !1),
        t(`showOthers`, !1),
        t(`multiple`, null),
        t(`customPX`, null),
        t(`customPY`, null));
      var r = [`inputField`, `displayArea`, `button`];
      for (var a in r)
        typeof e[r[a]] == `string` &&
          (e[r[a]] = document.getElementById(e[r[a]]));
      if (!(e.flat || e.multiple || e.inputField || e.displayArea || e.button))
        return (
          alert(`Calendar.setup:
  Nothing to setup (no fields found).  Please check your code`),
          !1
        );
      function o(e) {
        var t = e.params,
          r = e.dateClicked || t.electric;
        (r &&
          t.inputField &&
          ((t.inputField.value = e.date.print(t.ifFormat)),
          typeof t.inputField.onchange == `function` &&
            t.inputField.onchange()),
          r &&
            t.displayArea &&
            (t.displayArea.innerHTML = e.date.print(t.daFormat)),
          r && typeof t.onUpdate == `function` && t.onUpdate(e),
          r &&
            t.flat &&
            typeof t.flatCallback == `function` &&
            t.flatCallback(e),
          r && t.singleClick && e.dateClicked && e.callCloseHandler());
      }
      if (e.flat != null) {
        if (
          (typeof e.flat == `string` &&
            (e.flat = document.getElementById(e.flat)),
          !e.flat)
        )
          return (
            alert(`Calendar.setup:
  Flat specified but can't find parent.`),
            !1
          );
        var c = new Calendar(e.firstDay, e.date, e.onSelect || o);
        return (
          (c.showsOtherMonths = e.showOthers),
          (c.showsTime = e.showsTime),
          (c.time24 = e.timeFormat == `24`),
          (c.params = e),
          (c.weekNumbers = e.weekNumbers),
          c.setRange(e.range[0], e.range[1]),
          c.setDateStatusHandler(e.dateStatusFunc),
          (c.getDateText = e.dateText),
          e.ifFormat && c.setDateFormat(e.ifFormat),
          e.inputField &&
            typeof e.inputField.value == `string` &&
            c.parseDate(e.inputField.value),
          c.create(e.flat),
          c.show(),
          !1
        );
      }
      var l = e.button || e.displayArea || e.inputField;
      return (
        (l[`on` + e.eventName] = function () {
          var t = e.inputField || e.displayArea,
            r = e.inputField ? e.ifFormat : e.daFormat,
            a = !1,
            c = window.calendar;
          if (
            (t && (e.date = Date.parseDate(t.value || t.innerHTML, r)),
            c && e.cache
              ? (e.date && c.setDate(e.date), c.hide())
              : ((window.calendar = c =
                  new Calendar(
                    e.firstDay,
                    e.date,
                    e.onSelect || o,
                    e.onClose ||
                      function (e) {
                        e.hide();
                      }
                  )),
                (c.showsTime = e.showsTime),
                (c.time24 = e.timeFormat == `24`),
                (c.weekNumbers = e.weekNumbers),
                (a = !0)),
            e.multiple)
          ) {
            c.multiple = {};
            for (var l = e.multiple.length; --l >= 0;) {
              var u = e.multiple[l],
                f = u.print(`%Y%m%d`);
              c.multiple[f] = u;
            }
          }
          return (
            (c.showsOtherMonths = e.showOthers),
            (c.yearStep = e.step),
            c.setRange(e.range[0], e.range[1]),
            (c.params = e),
            c.setDateStatusHandler(e.dateStatusFunc),
            (c.getDateText = e.dateText),
            c.setDateFormat(r),
            a && c.create(),
            c.refresh(),
            e.position
              ? c.showAt(e.position[0], e.position[1])
              : e.customPX && e.customPY
                ? c.showAtElement(
                    e.button || e.displayArea || e.inputField,
                    e.align,
                    e.customPX,
                    e.customPY
                  )
                : c.showAtElement(
                    e.button || e.displayArea || e.inputField,
                    e.align
                  ),
            !1
          );
        }),
        c
      );
    };
  }),
  require_new_cal = __commonJSMin(() => {
    var e = require_calendar(),
      t = require_calendar_en(),
      r = require_calendar_setup();
  }),
  require_wz_tooltip = __commonJSMin(() => {
    var config = {},
      tt_Debug = !0,
      tt_Enabled = !0,
      TagsToTip = !0;
    ((config.Above = !1),
      (config.BgColor = `#E2E7FF`),
      (config.BgImg = ``),
      (config.BorderColor = `#003099`),
      (config.BorderStyle = `solid`),
      (config.BorderWidth = 1),
      (config.CenterMouse = !1),
      (config.ClickClose = !1),
      (config.ClickSticky = !1),
      (config.CloseBtn = !1),
      (config.CloseBtnColors = [`#990000`, `#FFFFFF`, `#DD3333`, `#FFFFFF`]),
      (config.CloseBtnText = `&nbsp;X&nbsp;`),
      (config.CopyContent = !0),
      (config.Delay = 400),
      (config.Duration = 0),
      (config.Exclusive = !1),
      (config.FadeIn = 100),
      (config.FadeOut = 100),
      (config.FadeInterval = 30),
      (config.Fix = null),
      (config.FollowMouse = !0),
      (config.FontColor = `#000044`),
      (config.FontFace = `Verdana,Geneva,sans-serif`),
      (config.FontSize = `8pt`),
      (config.FontWeight = `normal`),
      (config.Height = 0),
      (config.JumpHorz = !1),
      (config.JumpVert = !0),
      (config.Left = !1),
      (config.OffsetX = 14),
      (config.OffsetY = 8),
      (config.Opacity = 100),
      (config.Padding = 3),
      (config.Shadow = !1),
      (config.ShadowColor = `#C0C0C0`),
      (config.ShadowWidth = 5),
      (config.Sticky = !1),
      (config.TextAlign = `left`),
      (config.Title = ``),
      (config.TitleAlign = `left`),
      (config.TitleBgColor = ``),
      (config.TitleFontColor = `#FFFFFF`),
      (config.TitleFontFace = ``),
      (config.TitleFontSize = ``),
      (config.TitlePadding = 2),
      (config.Width = 0));
    function Tip() {
      tt_Tip(arguments, null);
    }
    function TagToTip() {
      var e = tt_GetElt(arguments[0]);
      e && tt_Tip(arguments, e);
    }
    function UnTip() {
      (tt_OpReHref(),
        tt_aV[DURATION] < 0 && tt_iState & 2
          ? tt_tDurt.Timer(`tt_HideInit()`, -tt_aV[DURATION], !0)
          : (tt_aV[STICKY] && tt_iState & 2) || tt_HideInit());
    }
    var tt_aElt = Array(10),
      tt_aV = [],
      tt_sContent,
      tt_t2t,
      tt_t2tDad,
      tt_musX,
      tt_musY,
      tt_over,
      tt_x,
      tt_y,
      tt_w,
      tt_h;
    function tt_Extension() {
      return (tt_ExtCmdEnum(), (tt_aExt[tt_aExt.length] = this), this);
    }
    function tt_SetTipPos(e, t) {
      var r = tt_aElt[0].style;
      if (((r.left = e + `px`), (r.top = t + `px`), tt_ie56)) {
        var a = tt_aElt[tt_aElt.length - 1];
        a && ((a.style.left = r.left), (a.style.top = r.top));
      }
    }
    function tt_HideInit() {
      if (tt_iState) {
        if (
          (tt_ExtCallFncs(0, `HideInit`),
          (tt_iState &= -13),
          tt_flagOpa && tt_aV[FADEOUT] && (tt_tFade.EndTimer(), tt_opa))
        ) {
          var e = Math.round(
            tt_aV[FADEOUT] / (tt_aV[FADEINTERVAL] * (tt_aV[OPACITY] / tt_opa))
          );
          tt_Fade(tt_opa, tt_opa, 0, e);
          return;
        }
        tt_tHide.Timer(`tt_Hide();`, 1, !1);
      }
    }
    function tt_Hide() {
      tt_db &&
        tt_iState &&
        (tt_OpReHref(),
        tt_iState & 2 &&
          ((tt_aElt[0].style.visibility = `hidden`), tt_ExtCallFncs(0, `Hide`)),
        tt_tShow.EndTimer(),
        tt_tHide.EndTimer(),
        tt_tDurt.EndTimer(),
        tt_tFade.EndTimer(),
        !tt_op && !tt_ie && (tt_tWaitMov.EndTimer(), (tt_bWait = !1)),
        (tt_aV[CLICKCLOSE] || tt_aV[CLICKSTICKY]) &&
          tt_RemEvtFnc(document, `mouseup`, tt_OnLClick),
        tt_ExtCallFncs(0, `Kill`),
        tt_t2t && !tt_aV[COPYCONTENT] && tt_UnEl2Tip(),
        (tt_iState = 0),
        (tt_over = null),
        tt_ResetMainDiv(),
        tt_aElt[tt_aElt.length - 1] &&
          (tt_aElt[tt_aElt.length - 1].style.display = `none`));
    }
    function tt_GetElt(e) {
      return document.getElementById
        ? document.getElementById(e)
        : document.all
          ? document.all[e]
          : null;
    }
    function tt_GetDivW(e) {
      return (e && (e.offsetWidth || e.style.pixelWidth)) || 0;
    }
    function tt_GetDivH(e) {
      return (e && (e.offsetHeight || e.style.pixelHeight)) || 0;
    }
    function tt_GetScrollX() {
      return window.pageXOffset || (tt_db && tt_db.scrollLeft) || 0;
    }
    function tt_GetScrollY() {
      return window.pageYOffset || (tt_db && tt_db.scrollTop) || 0;
    }
    function tt_GetClientW() {
      return tt_GetWndCliSiz(`Width`);
    }
    function tt_GetClientH() {
      return tt_GetWndCliSiz(`Height`);
    }
    function tt_GetEvtX(e) {
      return e
        ? typeof e.pageX == tt_u
          ? e.clientX + tt_GetScrollX()
          : e.pageX
        : 0;
    }
    function tt_GetEvtY(e) {
      return e
        ? typeof e.pageY == tt_u
          ? e.clientY + tt_GetScrollY()
          : e.pageY
        : 0;
    }
    function tt_AddEvtFnc(e, t, r) {
      e &&
        (e.addEventListener
          ? e.addEventListener(t, r, !1)
          : e.attachEvent(`on` + t, r));
    }
    function tt_RemEvtFnc(e, t, r) {
      e &&
        (e.removeEventListener
          ? e.removeEventListener(t, r, !1)
          : e.detachEvent(`on` + t, r));
    }
    function tt_GetDad(e) {
      return e.parentNode || e.parentElement || e.offsetParent;
    }
    function tt_MovDomNode(e, t, r) {
      (t && t.removeChild(e), r && r.appendChild(e));
    }
    var tt_aExt = [],
      tt_db,
      tt_op,
      tt_ie,
      tt_ie56,
      tt_bBoxOld,
      tt_body,
      tt_ovr_,
      tt_flagOpa,
      tt_maxPosX,
      tt_maxPosY,
      tt_iState = 0,
      tt_opa,
      tt_bJmpVert,
      tt_bJmpHorz,
      tt_elDeHref,
      tt_tShow = new Number(0),
      tt_tHide = new Number(0),
      tt_tDurt = new Number(0),
      tt_tFade = new Number(0),
      tt_tWaitMov = new Number(0),
      tt_bWait = !1,
      tt_u = `undefined`;
    function tt_Init() {
      (tt_MkCmdEnum(),
        !(!tt_Browser() || !tt_MkMainDiv()) &&
          (tt_IsW3cBox(),
          tt_OpaSupport(),
          tt_AddEvtFnc(document, `mousemove`, tt_Move),
          (TagsToTip || tt_Debug) && tt_SetOnloadFnc(),
          tt_AddEvtFnc(window, `unload`, tt_Hide)));
    }
    function tt_MkCmdEnum() {
      var n = 0;
      for (var i in config)
        eval(`window.` + i.toString().toUpperCase() + ` = ` + n++);
      tt_aV.length = n;
    }
    function tt_Browser() {
      var n = navigator.userAgent.toLowerCase(),
        nv = navigator.appVersion,
        n6,
        w3c;
      if (
        ((tt_op = document.defaultView && typeof eval(`window.opera`) != tt_u),
        (tt_ie = n.indexOf(`msie`) != -1 && document.all && !tt_op),
        tt_ie
          ? ((tt_db =
              !document.compatMode || document.compatMode == `BackCompat`
                ? document.body || null
                : document.documentElement),
            tt_db &&
              (tt_ie56 =
                parseFloat(nv.substring(nv.indexOf(`MSIE`) + 5)) >= 5.5 &&
                typeof document.body.style.maxHeight == tt_u))
          : ((tt_db =
              document.documentElement ||
              document.body ||
              (document.getElementsByTagName
                ? document.getElementsByTagName(`body`)[0]
                : null)),
            tt_op ||
              ((n6 =
                document.defaultView &&
                typeof document.defaultView.getComputedStyle != tt_u),
              (w3c = !n6 && document.getElementById))),
        (tt_body = document.getElementsByTagName
          ? document.getElementsByTagName(`body`)[0]
          : document.body || null),
        tt_ie || n6 || tt_op || w3c)
      )
        if (tt_body && tt_db) {
          if (document.attachEvent || document.addEventListener) return !0;
        } else
          tt_Err(
            `wz_tooltip.js must be included INSIDE the body section, immediately after the opening <body> tag.`,
            !1
          );
      return ((tt_db = null), !1);
    }
    function tt_MkMainDiv() {
      return (
        tt_body.insertAdjacentHTML
          ? tt_body.insertAdjacentHTML(`afterBegin`, tt_MkMainDivHtm())
          : typeof tt_body.innerHTML != tt_u &&
            document.createElement &&
            tt_body.appendChild &&
            tt_body.appendChild(tt_MkMainDivDom()),
        window.tt_GetMainDivRefs && tt_GetMainDivRefs()
          ? !0
          : ((tt_db = null), !1)
      );
    }
    function tt_MkMainDivHtm() {
      return (
        `<div id="WzTtDiV"></div>` +
        (tt_ie56
          ? `<iframe id="WzTtIfRm" src="javascript:false" scrolling="no" frameborder="0" style="filter:Alpha(opacity=0);position:absolute;top:0px;left:0px;display:none;"></iframe>`
          : ``)
      );
    }
    function tt_MkMainDivDom() {
      var e = document.createElement(`div`);
      return (e && (e.id = `WzTtDiV`), e);
    }
    function tt_GetMainDivRefs() {
      if (
        ((tt_aElt[0] = tt_GetElt(`WzTtDiV`)),
        tt_ie56 &&
          tt_aElt[0] &&
          ((tt_aElt[tt_aElt.length - 1] = tt_GetElt(`WzTtIfRm`)),
          tt_aElt[tt_aElt.length - 1] || (tt_aElt[0] = null)),
        tt_aElt[0])
      ) {
        var e = tt_aElt[0].style;
        return (
          (e.visibility = `hidden`),
          (e.position = `absolute`),
          (e.overflow = `hidden`),
          !0
        );
      }
      return !1;
    }
    function tt_ResetMainDiv() {
      (tt_SetTipPos(0, 0),
        (tt_aElt[0].innerHTML = ``),
        (tt_aElt[0].style.width = `0px`),
        (tt_h = 0));
    }
    function tt_IsW3cBox() {
      var e = tt_aElt[0].style;
      ((e.padding = `10px`),
        (e.width = `40px`),
        (tt_bBoxOld = tt_GetDivW(tt_aElt[0]) == 40),
        (e.padding = `0px`),
        tt_ResetMainDiv());
    }
    function tt_OpaSupport() {
      var e = tt_body.style;
      tt_flagOpa =
        typeof e.KhtmlOpacity == tt_u
          ? typeof e.KHTMLOpacity == tt_u
            ? typeof e.MozOpacity == tt_u
              ? typeof e.opacity == tt_u
                ? typeof e.filter == tt_u
                  ? 0
                  : 1
                : 5
              : 4
            : 3
          : 2;
    }
    function tt_SetOnloadFnc() {
      if (
        (tt_AddEvtFnc(document, `DOMContentLoaded`, tt_HideSrcTags),
        tt_AddEvtFnc(window, `load`, tt_HideSrcTags),
        tt_body.attachEvent &&
          tt_body.attachEvent(`onreadystatechange`, function () {
            tt_body.readyState == `complete` && tt_HideSrcTags();
          }),
        /WebKit|KHTML/i.test(navigator.userAgent))
      )
        var e = setInterval(function () {
          /loaded|complete/.test(document.readyState) &&
            (clearInterval(e), tt_HideSrcTags());
        }, 10);
    }
    function tt_HideSrcTags() {
      !window.tt_HideSrcTags ||
        window.tt_HideSrcTags.done ||
        ((window.tt_HideSrcTags.done = !0),
        tt_HideSrcTagsRecurs(tt_body) ||
          tt_Err(
            `There are HTML elements to be converted to tooltips.
If you want these HTML elements to be automatically hidden, you must edit wz_tooltip.js, and set TagsToTip in the global tooltip configuration to true.`,
            !0
          ));
    }
    function tt_HideSrcTagsRecurs(e) {
      for (
        var t, r, a = e.childNodes || e.children || null, o = a ? a.length : 0;
        o;
      )
        if (
          (--o,
          !tt_HideSrcTagsRecurs(a[o]) ||
            ((t = a[o].getAttribute
              ? a[o].getAttribute(`onmouseover`) || a[o].getAttribute(`onclick`)
              : typeof a[o].onmouseover == `function`
                ? a[o].onmouseover || a[o].onclick
                : null),
            t &&
              ((r = t.toString().match(/TagToTip\s*\(\s*'[^'.]+'\s*[\),]/)),
              r && r.length && !tt_HideSrcTag(r[0]))))
        )
          return !1;
      return !0;
    }
    function tt_HideSrcTag(e) {
      var t = e.replace(/.+'([^'.]+)'.+/, `$1`),
        r = tt_GetElt(t);
      if (r) {
        if (tt_Debug && !TagsToTip) return !1;
        r.style.display = `none`;
      } else
        tt_Err(
          `Invalid ID
'` +
            t +
            `'
passed to TagToTip(). There exists no HTML element with that ID.`,
          !0
        );
      return !0;
    }
    function tt_Tip(e, t) {
      !tt_db ||
        tt_iState & 8 ||
        (tt_iState && tt_Hide(),
        tt_Enabled &&
          ((tt_t2t = t),
          tt_ReadCmds(e) &&
            ((tt_iState = 5),
            tt_AdaptConfig1(),
            tt_MkTipContent(e),
            tt_MkTipSubDivs(),
            tt_FormatTip(),
            (tt_bJmpVert = !1),
            (tt_bJmpHorz = !1),
            (tt_maxPosX = tt_GetClientW() + tt_GetScrollX() - tt_w - 1),
            (tt_maxPosY = tt_GetClientH() + tt_GetScrollY() - tt_h - 1),
            tt_AdaptConfig2(),
            tt_OverInit(),
            tt_ShowInit(),
            tt_Move())));
    }
    function tt_ReadCmds(e) {
      var t = 0;
      for (var r in config) tt_aV[t++] = config[r];
      if (e.length & 1) {
        for (t = e.length - 1; t > 0; t -= 2) tt_aV[e[t - 1]] = e[t];
        return !0;
      }
      return (
        tt_Err(
          `Incorrect call of Tip() or TagToTip().
Each command must be followed by a value.`,
          !0
        ),
        !1
      );
    }
    function tt_AdaptConfig1() {
      if (
        (tt_ExtCallFncs(0, `LoadConfig`),
        tt_aV[TITLEBGCOLOR].length ||
          (tt_aV[TITLEBGCOLOR] = tt_aV[BORDERCOLOR]),
        tt_aV[TITLEFONTCOLOR].length ||
          (tt_aV[TITLEFONTCOLOR] = tt_aV[BGCOLOR]),
        tt_aV[TITLEFONTFACE].length || (tt_aV[TITLEFONTFACE] = tt_aV[FONTFACE]),
        tt_aV[TITLEFONTSIZE].length || (tt_aV[TITLEFONTSIZE] = tt_aV[FONTSIZE]),
        tt_aV[CLOSEBTN])
      ) {
        tt_aV[CLOSEBTNCOLORS] || (tt_aV[CLOSEBTNCOLORS] = [``, ``, ``, ``]);
        for (var e = 4; e;)
          (--e,
            tt_aV[CLOSEBTNCOLORS][e].length ||
              (tt_aV[CLOSEBTNCOLORS][e] =
                e & 1 ? tt_aV[TITLEFONTCOLOR] : tt_aV[TITLEBGCOLOR]));
        tt_aV[TITLE].length || (tt_aV[TITLE] = ` `);
      }
      (tt_aV[OPACITY] == 100 &&
        typeof tt_aElt[0].style.MozOpacity != tt_u &&
        !Array.every &&
        (tt_aV[OPACITY] = 99),
        tt_aV[FADEIN] &&
          tt_flagOpa &&
          tt_aV[DELAY] > 100 &&
          (tt_aV[DELAY] = Math.max(tt_aV[DELAY] - tt_aV[FADEIN], 100)));
    }
    function tt_AdaptConfig2() {
      tt_aV[CENTERMOUSE] &&
        ((tt_aV[OFFSETX] -=
          (tt_w - (tt_aV[SHADOW] ? tt_aV[SHADOWWIDTH] : 0)) >> 1),
        (tt_aV[JUMPHORZ] = !1));
    }
    function tt_MkTipContent(e) {
      ((tt_sContent = tt_t2t
        ? tt_aV[COPYCONTENT]
          ? tt_t2t.innerHTML
          : ``
        : e[0]),
        tt_ExtCallFncs(0, `CreateContentString`));
    }
    function tt_MkTipSubDivs() {
      var e = `position:relative;margin:0px;padding:0px;border-width:0px;left:0px;top:0px;line-height:normal;width:auto;`,
        t =
          ` cellspacing="0" cellpadding="0" border="0" style="` +
          e +
          `"><tbody style="` +
          e +
          `"><tr><td `;
      ((tt_aElt[0].style.width = tt_GetClientW() + `px`),
        (tt_aElt[0].innerHTML =
          (tt_aV[TITLE].length
            ? `<div id="WzTiTl" style="position:relative;z-index:1;"><table id="WzTiTlTb"` +
              t +
              `id="WzTiTlI" style="` +
              e +
              `">` +
              tt_aV[TITLE] +
              `</td>` +
              (tt_aV[CLOSEBTN]
                ? `<td align="right" style="` +
                  e +
                  `text-align:right;"><span id="WzClOsE" style="position:relative;left:2px;padding-left:2px;padding-right:2px;cursor:` +
                  (tt_ie ? `hand` : `pointer`) +
                  `;" onmouseover="tt_OnCloseBtnOver(1)" onmouseout="tt_OnCloseBtnOver(0)" onclick="tt_HideInit()">` +
                  tt_aV[CLOSEBTNTEXT] +
                  `</span></td>`
                : ``) +
              `</tr></tbody></table></div>`
            : ``) +
          `<div id="WzBoDy" style="position:relative;z-index:0;"><table` +
          t +
          `id="WzBoDyI" style="` +
          e +
          `">` +
          tt_sContent +
          `</td></tr></tbody></table></div>` +
          (tt_aV[SHADOW]
            ? `<div id="WzTtShDwR" style="position:absolute;overflow:hidden;"></div><div id="WzTtShDwB" style="position:relative;overflow:hidden;"></div>`
            : ``)),
        tt_GetSubDivRefs(),
        tt_t2t && !tt_aV[COPYCONTENT] && tt_El2Tip(),
        tt_ExtCallFncs(0, `SubDivsCreated`));
    }
    function tt_GetSubDivRefs() {
      for (
        var e = [
            `WzTiTl`,
            `WzTiTlTb`,
            `WzTiTlI`,
            `WzClOsE`,
            `WzBoDy`,
            `WzBoDyI`,
            `WzTtShDwB`,
            `WzTtShDwR`,
          ],
          t = e.length;
        t;
        --t
      )
        tt_aElt[t] = tt_GetElt(e[t - 1]);
    }
    function tt_FormatTip() {
      var e,
        t,
        r,
        a = tt_aV[PADDING],
        o,
        c = tt_aV[BORDERWIDTH],
        l,
        u,
        f = (a + c) << 1;
      (tt_aV[TITLE].length
        ? ((o = tt_aV[TITLEPADDING]),
          (e = tt_aElt[1].style),
          (e.background = tt_aV[TITLEBGCOLOR]),
          (e.paddingTop = e.paddingBottom = o + `px`),
          (e.paddingLeft = e.paddingRight = o + 2 + `px`),
          (e = tt_aElt[3].style),
          (e.color = tt_aV[TITLEFONTCOLOR]),
          tt_aV[WIDTH] == -1 && (e.whiteSpace = `nowrap`),
          (e.fontFamily = tt_aV[TITLEFONTFACE]),
          (e.fontSize = tt_aV[TITLEFONTSIZE]),
          (e.fontWeight = `bold`),
          (e.textAlign = tt_aV[TITLEALIGN]),
          tt_aElt[4] &&
            ((e = tt_aElt[4].style),
            (e.background = tt_aV[CLOSEBTNCOLORS][0]),
            (e.color = tt_aV[CLOSEBTNCOLORS][1]),
            (e.fontFamily = tt_aV[TITLEFONTFACE]),
            (e.fontSize = tt_aV[TITLEFONTSIZE]),
            (e.fontWeight = `bold`)),
          tt_aV[WIDTH] > 0
            ? (tt_w = tt_aV[WIDTH])
            : ((tt_w = tt_GetDivW(tt_aElt[3]) + tt_GetDivW(tt_aElt[4])),
              tt_aElt[4] && (tt_w += a),
              tt_aV[WIDTH] < -1 &&
                tt_w > -tt_aV[WIDTH] &&
                (tt_w = -tt_aV[WIDTH])),
          (l = -c))
        : ((tt_w = 0), (l = 0)),
        (e = tt_aElt[5].style),
        (e.top = l + `px`),
        c &&
          ((e.borderColor = tt_aV[BORDERCOLOR]),
          (e.borderStyle = tt_aV[BORDERSTYLE]),
          (e.borderWidth = c + `px`)),
        tt_aV[BGCOLOR].length && (e.background = tt_aV[BGCOLOR]),
        tt_aV[BGIMG].length &&
          (e.backgroundImage = `url(` + tt_aV[BGIMG] + `)`),
        (e.padding = a + `px`),
        (e.textAlign = tt_aV[TEXTALIGN]),
        tt_aV[HEIGHT] &&
          ((e.overflow = `auto`),
          tt_aV[HEIGHT] > 0
            ? (e.height = tt_aV[HEIGHT] + f + `px`)
            : (tt_h = f - tt_aV[HEIGHT])),
        (e = tt_aElt[6].style),
        (e.color = tt_aV[FONTCOLOR]),
        (e.fontFamily = tt_aV[FONTFACE]),
        (e.fontSize = tt_aV[FONTSIZE]),
        (e.fontWeight = tt_aV[FONTWEIGHT]),
        (e.textAlign = tt_aV[TEXTALIGN]),
        tt_aV[WIDTH] > 0
          ? (t = tt_aV[WIDTH])
          : tt_aV[WIDTH] == -1 && tt_w
            ? (t = tt_w)
            : ((t = tt_GetDivW(tt_aElt[6])),
              tt_aV[WIDTH] < -1 && t > -tt_aV[WIDTH] && (t = -tt_aV[WIDTH])),
        t > tt_w && (tt_w = t),
        (tt_w += f),
        tt_aV[SHADOW]
          ? ((tt_w += tt_aV[SHADOWWIDTH]),
            (u = Math.floor((tt_aV[SHADOWWIDTH] * 4) / 3)),
            (e = tt_aElt[7].style),
            (e.top = l + `px`),
            (e.left = u + `px`),
            (e.width = tt_w - u - tt_aV[SHADOWWIDTH] + `px`),
            (e.height = tt_aV[SHADOWWIDTH] + `px`),
            (e.background = tt_aV[SHADOWCOLOR]),
            (e = tt_aElt[8].style),
            (e.top = u + `px`),
            (e.left = tt_w - tt_aV[SHADOWWIDTH] + `px`),
            (e.width = tt_aV[SHADOWWIDTH] + `px`),
            (e.background = tt_aV[SHADOWCOLOR]))
          : (u = 0),
        tt_SetTipOpa(tt_aV[FADEIN] ? 0 : tt_aV[OPACITY]),
        tt_FixSize(l, u));
    }
    function tt_FixSize(e, t) {
      var r,
        a,
        o,
        c,
        l = tt_aV[PADDING],
        u = tt_aV[BORDERWIDTH],
        f;
      ((tt_aElt[0].style.width = tt_w + `px`),
        (tt_aElt[0].style.pixelWidth = tt_w),
        (a = tt_w - (tt_aV[SHADOW] ? tt_aV[SHADOWWIDTH] : 0)),
        (r = a),
        tt_bBoxOld || (r -= (l + u) << 1),
        (tt_aElt[5].style.width = r + `px`),
        tt_aElt[1] &&
          ((r = a - ((tt_aV[TITLEPADDING] + 2) << 1)),
          tt_bBoxOld || (a = r),
          (tt_aElt[1].style.width = a + `px`),
          (tt_aElt[2].style.width = r + `px`)),
        tt_h &&
          ((o = tt_GetDivH(tt_aElt[5])),
          o > tt_h &&
            (tt_bBoxOld || (tt_h -= (l + u) << 1),
            (tt_aElt[5].style.height = tt_h + `px`))),
        (tt_h = tt_GetDivH(tt_aElt[0]) + e),
        tt_aElt[8] && (tt_aElt[8].style.height = tt_h - t + `px`),
        (f = tt_aElt.length - 1),
        tt_aElt[f] &&
          ((tt_aElt[f].style.width = tt_w + `px`),
          (tt_aElt[f].style.height = tt_h + `px`)));
    }
    function tt_DeAlt(e) {
      var t;
      if (
        e &&
        ((e.alt &&= ``),
        (e.title &&= ``),
        (t = e.childNodes || e.children || null),
        t)
      )
        for (var r = t.length; r;) tt_DeAlt(t[--r]);
    }
    function tt_OpDeHref(e) {
      if (tt_op)
        for (tt_elDeHref && tt_OpReHref(); e;) {
          if (e.hasAttribute && e.hasAttribute(`href`)) {
            ((e.t_href = e.getAttribute(`href`)),
              (e.t_stats = window.status),
              e.removeAttribute(`href`),
              (e.style.cursor = `hand`),
              tt_AddEvtFnc(e, `mousedown`, tt_OpReHref),
              (window.status = e.t_href),
              (tt_elDeHref = e));
            break;
          }
          e = tt_GetDad(e);
        }
    }
    function tt_OpReHref() {
      tt_elDeHref &&=
        (tt_elDeHref.setAttribute(`href`, tt_elDeHref.t_href),
        tt_RemEvtFnc(tt_elDeHref, `mousedown`, tt_OpReHref),
        (window.status = tt_elDeHref.t_stats),
        null);
    }
    function tt_El2Tip() {
      var e = tt_t2t.style;
      ((tt_t2t.t_cp = e.position),
        (tt_t2t.t_cl = e.left),
        (tt_t2t.t_ct = e.top),
        (tt_t2t.t_cd = e.display),
        (tt_t2tDad = tt_GetDad(tt_t2t)),
        tt_MovDomNode(tt_t2t, tt_t2tDad, tt_aElt[6]),
        (e.display = `block`),
        (e.position = `static`),
        (e.left = e.top = e.marginLeft = e.marginTop = `0px`));
    }
    function tt_UnEl2Tip() {
      var e = tt_t2t.style;
      ((e.display = tt_t2t.t_cd),
        tt_MovDomNode(tt_t2t, tt_GetDad(tt_t2t), tt_t2tDad),
        (e.position = tt_t2t.t_cp),
        (e.left = tt_t2t.t_cl),
        (e.top = tt_t2t.t_ct),
        (tt_t2tDad = null));
    }
    function tt_OverInit() {
      ((tt_over = window.event
        ? window.event.target || window.event.srcElement
        : tt_ovr_),
        tt_DeAlt(tt_over),
        tt_OpDeHref(tt_over));
    }
    function tt_ShowInit() {
      (tt_tShow.Timer(`tt_Show()`, tt_aV[DELAY], !0),
        (tt_aV[CLICKCLOSE] || tt_aV[CLICKSTICKY]) &&
          tt_AddEvtFnc(document, `mouseup`, tt_OnLClick));
    }
    function tt_Show() {
      var e = tt_aElt[0].style;
      ((e.zIndex = Math.max(window.dd && dd.z ? dd.z + 2 : 0, 1010)),
        (tt_aV[STICKY] || !tt_aV[FOLLOWMOUSE]) && (tt_iState &= -5),
        tt_aV[EXCLUSIVE] && (tt_iState |= 8),
        tt_aV[DURATION] > 0 &&
          tt_tDurt.Timer(`tt_HideInit()`, tt_aV[DURATION], !0),
        tt_ExtCallFncs(0, `Show`),
        (e.visibility = `visible`),
        (tt_iState |= 2),
        tt_aV[FADEIN] &&
          tt_Fade(
            0,
            0,
            tt_aV[OPACITY],
            Math.round(tt_aV[FADEIN] / tt_aV[FADEINTERVAL])
          ),
        tt_ShowIfrm());
    }
    function tt_ShowIfrm() {
      if (tt_ie56) {
        var e = tt_aElt[tt_aElt.length - 1];
        if (e) {
          var t = e.style;
          ((t.zIndex = tt_aElt[0].style.zIndex - 1), (t.display = `block`));
        }
      }
    }
    function tt_Move(e) {
      if (
        (e && (tt_ovr_ = e.target || e.srcElement),
        (e ||= window.event),
        e && ((tt_musX = tt_GetEvtX(e)), (tt_musY = tt_GetEvtY(e))),
        tt_iState & 4)
      ) {
        if (!tt_op && !tt_ie) {
          if (tt_bWait) return;
          ((tt_bWait = !0), tt_tWaitMov.Timer(`tt_bWait = false;`, 1, !0));
        }
        (tt_aV[FIX]
          ? ((tt_iState &= -5), tt_PosFix())
          : tt_ExtCallFncs(e, `MoveBefore`) ||
            tt_SetTipPos(tt_Pos(0), tt_Pos(1)),
          tt_ExtCallFncs([tt_musX, tt_musY], `MoveAfter`));
      }
    }
    function tt_Pos(e) {
      var t, r, a, o, c, l, u, f, p;
      return (
        e
          ? ((r = tt_aV[JUMPVERT]),
            (a = ABOVE),
            (o = OFFSETY),
            (c = tt_h),
            (l = tt_maxPosY),
            (u = tt_GetScrollY()),
            (f = tt_musY),
            (p = tt_bJmpVert))
          : ((r = tt_aV[JUMPHORZ]),
            (a = LEFT),
            (o = OFFSETX),
            (c = tt_w),
            (l = tt_maxPosX),
            (u = tt_GetScrollX()),
            (f = tt_musX),
            (p = tt_bJmpHorz)),
        r
          ? (t =
              (tt_aV[a] && (!p || tt_CalcPosAlt(e) >= u + 16)) ||
              (!tt_aV[a] && p && tt_CalcPosDef(e) > l - 16)
                ? tt_PosAlt(e)
                : tt_PosDef(e))
          : ((t = f),
            tt_aV[a]
              ? (t -= c + tt_aV[o] - (tt_aV[SHADOW] ? tt_aV[SHADOWWIDTH] : 0))
              : (t += tt_aV[o])),
        t > l && (t = r ? tt_PosAlt(e) : l),
        t < u && (t = r ? tt_PosDef(e) : u),
        t
      );
    }
    function tt_PosDef(e) {
      return (
        e ? (tt_bJmpVert = tt_aV[ABOVE]) : (tt_bJmpHorz = tt_aV[LEFT]),
        tt_CalcPosDef(e)
      );
    }
    function tt_PosAlt(e) {
      return (
        e ? (tt_bJmpVert = !tt_aV[ABOVE]) : (tt_bJmpHorz = !tt_aV[LEFT]),
        tt_CalcPosAlt(e)
      );
    }
    function tt_CalcPosDef(e) {
      return e ? tt_musY + tt_aV[OFFSETY] : tt_musX + tt_aV[OFFSETX];
    }
    function tt_CalcPosAlt(e) {
      var t = e ? OFFSETY : OFFSETX,
        r = tt_aV[t] - (tt_aV[SHADOW] ? tt_aV[SHADOWWIDTH] : 0);
      return (
        tt_aV[t] > 0 && r <= 0 && (r = 1),
        (e ? tt_musY - tt_h : tt_musX - tt_w) - r
      );
    }
    function tt_PosFix() {
      var e, t;
      if (typeof tt_aV[FIX][0] == `number`)
        ((e = tt_aV[FIX][0]), (t = tt_aV[FIX][1]));
      else
        for (
          el =
            typeof tt_aV[FIX][0] == `string`
              ? tt_GetElt(tt_aV[FIX][0])
              : tt_aV[FIX][0],
            e = tt_aV[FIX][1],
            t = tt_aV[FIX][2],
            !tt_aV[ABOVE] && el && (t += tt_GetDivH(el));
          el;
          el = el.offsetParent
        )
          ((e += el.offsetLeft || 0), (t += el.offsetTop || 0));
      (tt_aV[ABOVE] && (t -= tt_h), tt_SetTipPos(e, t));
    }
    function tt_Fade(e, t, r, a) {
      (a &&
        ((t += Math.round((r - t) / a)),
        (r > e ? t >= r : t <= r)
          ? (t = r)
          : tt_tFade.Timer(
              `tt_Fade(` + e + `,` + t + `,` + r + `,` + (a - 1) + `)`,
              tt_aV[FADEINTERVAL],
              !0
            )),
        t ? tt_SetTipOpa(t) : tt_Hide());
    }
    function tt_SetTipOpa(e) {
      (tt_SetOpa(tt_aElt[5], e),
        tt_aElt[1] && tt_SetOpa(tt_aElt[1], e),
        tt_aV[SHADOW] &&
          ((e = Math.round(e * 0.8)),
          tt_SetOpa(tt_aElt[7], e),
          tt_SetOpa(tt_aElt[8], e)));
    }
    function tt_OnCloseBtnOver(e) {
      var t = tt_aElt[4].style;
      ((e <<= 1),
        (t.background = tt_aV[CLOSEBTNCOLORS][e]),
        (t.color = tt_aV[CLOSEBTNCOLORS][e + 1]));
    }
    function tt_OnLClick(e) {
      ((e ||= window.event),
        (e.button && e.button & 2) ||
          (e.which && e.which == 3) ||
          (tt_aV[CLICKSTICKY] && tt_iState & 4
            ? ((tt_aV[STICKY] = !0), (tt_iState &= -5))
            : tt_aV[CLICKCLOSE] && tt_HideInit()));
    }
    function tt_Int(e) {
      var t;
      return isNaN((t = parseInt(e))) ? 0 : t;
    }
    ((Number.prototype.Timer = function (e, t, r) {
      (!this.value || r) && (this.value = window.setTimeout(e, t));
    }),
      (Number.prototype.EndTimer = function () {
        this.value &&= (window.clearTimeout(this.value), 0);
      }));
    function tt_GetWndCliSiz(e) {
      var t,
        r = window[`inner` + e],
        a = `client` + e,
        o = `number`;
      if (typeof r == o) {
        var c;
        return ((t = document.body) && typeof (c = t[a]) == o && c && c <= r) ||
          ((t = document.documentElement) &&
            typeof (c = t[a]) == o &&
            c &&
            c <= r)
          ? c
          : r;
      }
      return (t = document.documentElement) && (r = t[a])
        ? r
        : document.body[a];
    }
    function tt_SetOpa(e, t) {
      var r = e.style;
      if (((tt_opa = t), tt_flagOpa == 1))
        if (t < 100) {
          typeof e.filtNo == tt_u && (e.filtNo = r.filter);
          var a = r.visibility != `hidden`;
          ((r.zoom = `100%`),
            a || (r.visibility = `visible`),
            (r.filter = `alpha(opacity=` + t + `)`),
            a || (r.visibility = `hidden`));
        } else typeof e.filtNo != tt_u && (r.filter = e.filtNo);
      else
        switch (((t /= 100), tt_flagOpa)) {
          case 2:
            r.KhtmlOpacity = t;
            break;
          case 3:
            r.KHTMLOpacity = t;
            break;
          case 4:
            r.MozOpacity = t;
            break;
          case 5:
            r.opacity = t;
            break;
        }
    }
    function tt_Err(e, t) {
      (tt_Debug || !t) &&
        alert(
          `Tooltip Script Error Message:

` + e
        );
    }
    function tt_ExtCmdEnum() {
      var s;
      for (var i in config)
        ((s = `window.` + i.toString().toUpperCase()),
          eval(`typeof(` + s + `) == tt_u`) &&
            (eval(s + ` = ` + tt_aV.length), (tt_aV[tt_aV.length] = null)));
    }
    function tt_ExtCallFncs(e, t) {
      for (var r = !1, a = tt_aExt.length; a;) {
        --a;
        var o = tt_aExt[a][`On` + t];
        o && o(e) && (r = !0);
      }
      return r;
    }
    tt_Init();
  }),
  require_CalendarPopup = __commonJSMin(() => {
    function getAnchorPosition(e) {
      var t = !1,
        r = {},
        a = 0,
        o = 0,
        c = !1,
        l = !1,
        u = !1;
      if (
        (document.getElementById
          ? (c = !0)
          : document.all
            ? (l = !0)
            : document.layers && (u = !0),
        c && document.all)
      )
        ((a = AnchorPosition_getPageOffsetLeft(document.all[e])),
          (o = AnchorPosition_getPageOffsetTop(document.all[e])));
      else if (c) {
        var f = document.getElementById(e);
        ((a = AnchorPosition_getPageOffsetLeft(f)),
          (o = AnchorPosition_getPageOffsetTop(f)));
      } else if (l)
        ((a = AnchorPosition_getPageOffsetLeft(document.all[e])),
          (o = AnchorPosition_getPageOffsetTop(document.all[e])));
      else if (u) {
        for (var p = 0, m = 0; m < document.anchors.length; m++)
          if (document.anchors[m].name == e) {
            p = 1;
            break;
          }
        if (p == 0) return ((r.x = 0), (r.y = 0), r);
        ((a = document.anchors[m].x), (o = document.anchors[m].y));
      } else return ((r.x = 0), (r.y = 0), r);
      return ((r.x = a), (r.y = o), r);
    }
    function getAnchorWindowPosition(e) {
      var t = getAnchorPosition(e),
        r = 0,
        a = 0;
      return (
        document.getElementById
          ? isNaN(window.screenX)
            ? ((r = t.x - document.body.scrollLeft + window.screenLeft),
              (a = t.y - document.body.scrollTop + window.screenTop))
            : ((r =
                t.x +
                window.screenX +
                (window.outerWidth - window.innerWidth) -
                window.pageXOffset),
              (a =
                t.y +
                window.screenY +
                (window.outerHeight - 24 - window.innerHeight) -
                window.pageYOffset))
          : document.all
            ? ((r = t.x - document.body.scrollLeft + window.screenLeft),
              (a = t.y - document.body.scrollTop + window.screenTop))
            : document.layers &&
              ((r =
                t.x +
                window.screenX +
                (window.outerWidth - window.innerWidth) -
                window.pageXOffset),
              (a =
                t.y +
                window.screenY +
                (window.outerHeight - 24 - window.innerHeight) -
                window.pageYOffset)),
        (t.x = r),
        (t.y = a),
        t
      );
    }
    function AnchorPosition_getPageOffsetLeft(e) {
      for (var t = e.offsetLeft; (e = e.offsetParent) != null;)
        t += e.offsetLeft;
      return t;
    }
    function AnchorPosition_getWindowOffsetLeft(e) {
      return AnchorPosition_getPageOffsetLeft(e) - document.body.scrollLeft;
    }
    function AnchorPosition_getPageOffsetTop(e) {
      for (var t = e.offsetTop; (e = e.offsetParent) != null;) t += e.offsetTop;
      return t;
    }
    function AnchorPosition_getWindowOffsetTop(e) {
      return AnchorPosition_getPageOffsetTop(e) - document.body.scrollTop;
    }
    var MONTH_NAMES = [
        `January`,
        `February`,
        `March`,
        `April`,
        `May`,
        `June`,
        `July`,
        `August`,
        `September`,
        `October`,
        `November`,
        `December`,
        `Jan`,
        `Feb`,
        `Mar`,
        `Apr`,
        `May`,
        `Jun`,
        `Jul`,
        `Aug`,
        `Sep`,
        `Oct`,
        `Nov`,
        `Dec`,
      ],
      DAY_NAMES = [
        `Sunday`,
        `Monday`,
        `Tuesday`,
        `Wednesday`,
        `Thursday`,
        `Friday`,
        `Saturday`,
        `Sun`,
        `Mon`,
        `Tue`,
        `Wed`,
        `Thu`,
        `Fri`,
        `Sat`,
      ];
    function LZ(e) {
      return (e < 0 || e > 9 ? `` : `0`) + e;
    }
    function isDate(e, t) {
      return getDateFromFormat(e, t) != 0;
    }
    function compareDates(e, t, r, a) {
      var o = getDateFromFormat(e, t),
        c = getDateFromFormat(r, a);
      return o == 0 || c == 0 ? -1 : +(o > c);
    }
    function formatDate(e, t) {
      t += ``;
      var r = ``,
        a = 0,
        o = ``,
        c = ``,
        l = e.getYear() + ``,
        u = e.getMonth() + 1,
        f = e.getDate(),
        p = e.getDay(),
        m = e.getHours(),
        h = e.getMinutes(),
        g = e.getSeconds(),
        v,
        y,
        b,
        x,
        S,
        C,
        w,
        T,
        E,
        D,
        O,
        m,
        k,
        A,
        M,
        N,
        P = {};
      for (
        l.length < 4 && (l = `` + (l - 0 + 1900)),
          P.y = `` + l,
          P.yyyy = l,
          P.yy = l.substring(2, 4),
          P.M = u,
          P.MM = LZ(u),
          P.MMM = MONTH_NAMES[u - 1],
          P.NNN = MONTH_NAMES[u + 11],
          P.d = f,
          P.dd = LZ(f),
          P.E = DAY_NAMES[p + 7],
          P.EE = DAY_NAMES[p],
          P.H = m,
          P.HH = LZ(m),
          m == 0 ? (P.h = 12) : m > 12 ? (P.h = m - 12) : (P.h = m),
          P.hh = LZ(P.h),
          m > 11 ? (P.K = m - 12) : (P.K = m),
          P.k = m + 1,
          P.KK = LZ(P.K),
          P.kk = LZ(P.k),
          m > 11 ? (P.a = `PM`) : (P.a = `AM`),
          P.m = h,
          P.mm = LZ(h),
          P.s = g,
          P.ss = LZ(g);
        a < t.length;
      ) {
        for (o = t.charAt(a), c = ``; t.charAt(a) == o && a < t.length;)
          c += t.charAt(a++);
        P[c] == null ? (r += c) : (r += P[c]);
      }
      return r;
    }
    function _isInteger(e) {
      for (var t = `1234567890`, r = 0; r < e.length; r++)
        if (t.indexOf(e.charAt(r)) == -1) return !1;
      return !0;
    }
    function _getInt(e, t, r, a) {
      for (var o = a; o >= r; o--) {
        var c = e.substring(t, t + o);
        if (c.length < r) return null;
        if (_isInteger(c)) return c;
      }
      return null;
    }
    function getDateFromFormat(e, t) {
      ((e += ``), (t += ``));
      for (
        var r = 0,
          a = 0,
          o = ``,
          c = ``,
          l = ``,
          u,
          f,
          p = new Date(),
          m = p.getYear(),
          h = p.getMonth() + 1,
          g = 1,
          v = p.getHours(),
          y = p.getMinutes(),
          b = p.getSeconds(),
          x = ``;
        a < t.length;
      ) {
        for (o = t.charAt(a), c = ``; t.charAt(a) == o && a < t.length;)
          c += t.charAt(a++);
        if (c == `yyyy` || c == `yy` || c == `y`) {
          if (
            (c == `yyyy` && ((u = 4), (f = 4)),
            c == `yy` && ((u = 2), (f = 2)),
            c == `y` && ((u = 2), (f = 4)),
            (m = _getInt(e, r, u, f)),
            m == null)
          )
            return 0;
          ((r += m.length),
            m.length == 2 && (m = m > 70 ? 1900 + (m - 0) : 2e3 + (m - 0)));
        } else if (c == `MMM` || c == `NNN`) {
          h = 0;
          for (var S = 0; S < MONTH_NAMES.length; S++) {
            var C = MONTH_NAMES[S];
            if (
              e.substring(r, r + C.length).toLowerCase() == C.toLowerCase() &&
              (c == `MMM` || (c == `NNN` && S > 11))
            ) {
              ((h = S + 1), h > 12 && (h -= 12), (r += C.length));
              break;
            }
          }
          if (h < 1 || h > 12) return 0;
        } else if (c == `EE` || c == `E`)
          for (var S = 0; S < DAY_NAMES.length; S++) {
            var w = DAY_NAMES[S];
            if (e.substring(r, r + w.length).toLowerCase() == w.toLowerCase()) {
              r += w.length;
              break;
            }
          }
        else if (c == `MM` || c == `M`) {
          if (((h = _getInt(e, r, c.length, 2)), h == null || h < 1 || h > 12))
            return 0;
          r += h.length;
        } else if (c == `dd` || c == `d`) {
          if (((g = _getInt(e, r, c.length, 2)), g == null || g < 1 || g > 31))
            return 0;
          r += g.length;
        } else if (c == `hh` || c == `h`) {
          if (((v = _getInt(e, r, c.length, 2)), v == null || v < 1 || v > 12))
            return 0;
          r += v.length;
        } else if (c == `HH` || c == `H`) {
          if (((v = _getInt(e, r, c.length, 2)), v == null || v < 0 || v > 23))
            return 0;
          r += v.length;
        } else if (c == `KK` || c == `K`) {
          if (((v = _getInt(e, r, c.length, 2)), v == null || v < 0 || v > 11))
            return 0;
          r += v.length;
        } else if (c == `kk` || c == `k`) {
          if (((v = _getInt(e, r, c.length, 2)), v == null || v < 1 || v > 24))
            return 0;
          ((r += v.length), v--);
        } else if (c == `mm` || c == `m`) {
          if (((y = _getInt(e, r, c.length, 2)), y == null || y < 0 || y > 59))
            return 0;
          r += y.length;
        } else if (c == `ss` || c == `s`) {
          if (((b = _getInt(e, r, c.length, 2)), b == null || b < 0 || b > 59))
            return 0;
          r += b.length;
        } else if (c == `a`) {
          if (e.substring(r, r + 2).toLowerCase() == `am`) x = `AM`;
          else if (e.substring(r, r + 2).toLowerCase() == `pm`) x = `PM`;
          else return 0;
          r += 2;
        } else if (e.substring(r, r + c.length) != c) return 0;
        else r += c.length;
      }
      if (r != e.length) return 0;
      if (h == 2) {
        if ((m % 4 == 0 && m % 100 != 0) || m % 400 == 0) {
          if (g > 29) return 0;
        } else if (g > 28) return 0;
      }
      return (h == 4 || h == 6 || h == 9 || h == 11) && g > 30
        ? 0
        : (v < 12 && x == `PM`
            ? (v = v - 0 + 12)
            : v > 11 && x == `AM` && (v -= 12),
          new Date(m, h - 1, g, v, y, b).getTime());
    }
    function parseDate(e) {
      var t = arguments.length == 2 ? arguments[1] : !1;
      ((generalFormats = [
        `y-M-d`,
        `MMM d, y`,
        `MMM d,y`,
        `y-MMM-d`,
        `d-MMM-y`,
        `MMM d`,
      ]),
        (monthFirst = [`M/d/y`, `M-d-y`, `M.d.y`, `MMM-d`, `M/d`, `M-d`]),
        (dateFirst = [`d/M/y`, `d-M-y`, `d.M.y`, `d-MMM`, `d/M`, `d-M`]));
      for (
        var r = [
            `generalFormats`,
            t ? `dateFirst` : `monthFirst`,
            t ? `monthFirst` : `dateFirst`,
          ],
          a = null,
          o = 0;
        o < r.length;
        o++
      )
        for (var c = window[r[o]], l = 0; l < c.length; l++)
          if (((a = getDateFromFormat(e, c[l])), a != 0)) return new Date(a);
      return null;
    }
    function PopupWindow_getXYPosition(e) {
      var t =
        this.type == `WINDOW`
          ? getAnchorWindowPosition(e)
          : getAnchorPosition(e);
      ((this.x = t.x), (this.y = t.y));
    }
    function PopupWindow_setSize(e, t) {
      ((this.width = e), (this.height = t));
    }
    function PopupWindow_populate(e) {
      ((this.contents = e), (this.populated = !1));
    }
    function PopupWindow_setUrl(e) {
      this.url = e;
    }
    function PopupWindow_setWindowProperties(e) {
      this.windowProperties = e;
    }
    function PopupWindow_refresh() {
      if (this.divName != null) {
        if (this.use_gebi)
          document.getElementById(this.divName).innerHTML = this.contents;
        else if (this.use_css)
          document.all[this.divName].innerHTML = this.contents;
        else if (this.use_layers) {
          var e = document.layers[this.divName];
          (e.document.open(),
            e.document.writeln(this.contents),
            e.document.close());
        }
      } else
        this.popupWindow != null &&
          !this.popupWindow.closed &&
          (this.url == ``
            ? (this.popupWindow.document.open(),
              this.popupWindow.document.writeln(this.contents),
              this.popupWindow.document.close())
            : (this.popupWindow.location.href = this.url),
          this.popupWindow.focus());
    }
    function PopupWindow_showPopup(e) {
      if (
        (this.getXYPosition(e),
        (this.x += this.offsetX),
        (this.y += this.offsetY),
        !this.populated &&
          this.contents != `` &&
          ((this.populated = !0), this.refresh()),
        this.divName != null)
      )
        this.use_gebi
          ? ((document.getElementById(this.divName).style.left = this.x + `px`),
            (document.getElementById(this.divName).style.top = this.y + `px`),
            (document.getElementById(this.divName).style.visibility =
              `visible`))
          : this.use_css
            ? ((document.all[this.divName].style.left = this.x),
              (document.all[this.divName].style.top = this.y),
              (document.all[this.divName].style.visibility = `visible`))
            : this.use_layers &&
              ((document.layers[this.divName].left = this.x),
              (document.layers[this.divName].top = this.y),
              (document.layers[this.divName].visibility = `visible`));
      else {
        if (this.popupWindow == null || this.popupWindow.closed) {
          (this.x < 0 && (this.x = 0),
            this.y < 0 && (this.y = 0),
            screen &&
              screen.availHeight &&
              this.y + this.height > screen.availHeight &&
              (this.y = screen.availHeight - this.height),
            screen &&
              screen.availWidth &&
              this.x + this.width > screen.availWidth &&
              (this.x = screen.availWidth - this.width));
          var t =
            window.opera ||
            (document.layers && !navigator.mimeTypes[`*`]) ||
            navigator.vendor == `KDE` ||
            (document.childNodes && !document.all && !navigator.taintEnabled);
          this.popupWindow = window.open(
            t ? `` : `about:blank`,
            `window_` + e,
            this.windowProperties +
              `,width=` +
              this.width +
              `,height=` +
              this.height +
              `,screenX=` +
              this.x +
              `,left=` +
              this.x +
              `,screenY=` +
              this.y +
              `,top=` +
              this.y
          );
        }
        this.refresh();
      }
    }
    function PopupWindow_hidePopup() {
      this.divName == null
        ? this.popupWindow &&
          !this.popupWindow.closed &&
          (this.popupWindow.close(), (this.popupWindow = null))
        : this.use_gebi
          ? (document.getElementById(this.divName).style.visibility = `hidden`)
          : this.use_css
            ? (document.all[this.divName].style.visibility = `hidden`)
            : this.use_layers &&
              (document.layers[this.divName].visibility = `hidden`);
    }
    function PopupWindow_isClicked(e) {
      if (this.divName != null) {
        if (this.use_layers) {
          var t = e.pageX,
            r = e.pageY,
            a = document.layers[this.divName];
          return (
            t > a.left &&
            t < a.left + a.clip.width &&
            r > a.top &&
            r < a.top + a.clip.height
          );
        } else if (document.all) {
          for (var a = window.event.srcElement; a.parentElement != null;) {
            if (a.id == this.divName) return !0;
            a = a.parentElement;
          }
          return !1;
        } else if (this.use_gebi && e) {
          for (var a = e.originalTarget; a.parentNode != null;) {
            if (a.id == this.divName) return !0;
            a = a.parentNode;
          }
          return !1;
        }
        return !1;
      }
      return !1;
    }
    function PopupWindow_hideIfNotClicked(e) {
      this.autoHideEnabled && !this.isClicked(e) && this.hidePopup();
    }
    function PopupWindow_autoHide() {
      this.autoHideEnabled = !0;
    }
    function PopupWindow_hidePopupWindows(e) {
      for (var t = 0; t < popupWindowObjects.length; t++)
        popupWindowObjects[t] != null &&
          popupWindowObjects[t].hideIfNotClicked(e);
    }
    function PopupWindow_attachListener() {
      (document.layers && document.captureEvents(Event.MOUSEUP),
        (window.popupWindowOldEventListener = document.onmouseup),
        window.popupWindowOldEventListener == null
          ? (document.onmouseup = PopupWindow_hidePopupWindows)
          : (document.onmouseup = Function(
              `window.popupWindowOldEventListener(); PopupWindow_hidePopupWindows();`
            )));
    }
    function PopupWindow() {
      (window.popupWindowIndex || (window.popupWindowIndex = 0),
        window.popupWindowObjects || (window.popupWindowObjects = []),
        window.listenerAttached ||
          ((window.listenerAttached = !0), PopupWindow_attachListener()),
        (this.index = popupWindowIndex++),
        (popupWindowObjects[this.index] = this),
        (this.divName = null),
        (this.popupWindow = null),
        (this.width = 0),
        (this.height = 0),
        (this.populated = !1),
        (this.visible = !1),
        (this.autoHideEnabled = !1),
        (this.contents = ``),
        (this.url = ``),
        (this.windowProperties = `toolbar=no,location=no,status=no,menubar=no,scrollbars=auto,resizable,alwaysRaised,dependent,titlebar=no`),
        arguments.length > 0
          ? ((this.type = `DIV`), (this.divName = arguments[0]))
          : (this.type = `WINDOW`),
        (this.use_gebi = !1),
        (this.use_css = !1),
        (this.use_layers = !1),
        document.getElementById
          ? (this.use_gebi = !0)
          : document.all
            ? (this.use_css = !0)
            : document.layers
              ? (this.use_layers = !0)
              : (this.type = `WINDOW`),
        (this.offsetX = 0),
        (this.offsetY = 0),
        (this.getXYPosition = PopupWindow_getXYPosition),
        (this.populate = PopupWindow_populate),
        (this.setUrl = PopupWindow_setUrl),
        (this.setWindowProperties = PopupWindow_setWindowProperties),
        (this.refresh = PopupWindow_refresh),
        (this.showPopup = PopupWindow_showPopup),
        (this.hidePopup = PopupWindow_hidePopup),
        (this.setSize = PopupWindow_setSize),
        (this.isClicked = PopupWindow_isClicked),
        (this.autoHide = PopupWindow_autoHide),
        (this.hideIfNotClicked = PopupWindow_hideIfNotClicked));
    }
    function CalendarPopup() {
      var e;
      return (
        arguments.length > 0
          ? (e = new PopupWindow(arguments[0]))
          : ((e = new PopupWindow()), e.setSize(150, 175)),
        (e.offsetX = -152),
        (e.offsetY = 25),
        e.autoHide(),
        (e.monthNames = [
          `January`,
          `February`,
          `March`,
          `April`,
          `May`,
          `June`,
          `July`,
          `August`,
          `September`,
          `October`,
          `November`,
          `December`,
        ]),
        (e.monthAbbreviations = [
          `Jan`,
          `Feb`,
          `Mar`,
          `Apr`,
          `May`,
          `Jun`,
          `Jul`,
          `Aug`,
          `Sep`,
          `Oct`,
          `Nov`,
          `Dec`,
        ]),
        (e.dayHeaders = [`S`, `M`, `T`, `W`, `T`, `F`, `S`]),
        (e.returnFunction = `CP_tmpReturnFunction`),
        (e.returnMonthFunction = `CP_tmpReturnMonthFunction`),
        (e.returnQuarterFunction = `CP_tmpReturnQuarterFunction`),
        (e.returnYearFunction = `CP_tmpReturnYearFunction`),
        (e.weekStartDay = 0),
        (e.isShowYearNavigation = !1),
        (e.displayType = `date`),
        (e.disabledWeekDays = {}),
        (e.disabledDatesExpression = ``),
        (e.yearSelectStartOffset = 2),
        (e.currentDate = null),
        (e.todayText = `Today`),
        (e.cssPrefix = ``),
        (e.isShowNavigationDropdowns = !1),
        (e.isShowYearNavigationInput = !1),
        (window.CP_calendarObject = null),
        (window.CP_targetInput = null),
        (window.CP_dateFormat = `MM/dd/yyyy`),
        (e.copyMonthNamesToWindow = CP_copyMonthNamesToWindow),
        (e.setReturnFunction = CP_setReturnFunction),
        (e.setReturnMonthFunction = CP_setReturnMonthFunction),
        (e.setReturnQuarterFunction = CP_setReturnQuarterFunction),
        (e.setReturnYearFunction = CP_setReturnYearFunction),
        (e.setMonthNames = CP_setMonthNames),
        (e.setMonthAbbreviations = CP_setMonthAbbreviations),
        (e.setDayHeaders = CP_setDayHeaders),
        (e.setWeekStartDay = CP_setWeekStartDay),
        (e.setDisplayType = CP_setDisplayType),
        (e.setDisabledWeekDays = CP_setDisabledWeekDays),
        (e.addDisabledDates = CP_addDisabledDates),
        (e.setYearSelectStartOffset = CP_setYearSelectStartOffset),
        (e.setTodayText = CP_setTodayText),
        (e.showYearNavigation = CP_showYearNavigation),
        (e.showCalendar = CP_showCalendar),
        (e.hideCalendar = CP_hideCalendar),
        (e.getStyles = getCalendarStyles),
        (e.refreshCalendar = CP_refreshCalendar),
        (e.getCalendar = CP_getCalendar),
        (e.select = CP_select),
        (e.setCssPrefix = CP_setCssPrefix),
        (e.showNavigationDropdowns = CP_showNavigationDropdowns),
        (e.showYearNavigationInput = CP_showYearNavigationInput),
        e.copyMonthNamesToWindow(),
        e
      );
    }
    function CP_copyMonthNamesToWindow() {
      if (window.MONTH_NAMES !== void 0 && window.MONTH_NAMES != null) {
        window.MONTH_NAMES = [];
        for (var e = 0; e < this.monthNames.length; e++)
          window.MONTH_NAMES[window.MONTH_NAMES.length] = this.monthNames[e];
        for (var e = 0; e < this.monthAbbreviations.length; e++)
          window.MONTH_NAMES[window.MONTH_NAMES.length] =
            this.monthAbbreviations[e];
      }
    }
    function CP_tmpReturnFunction(e, t, r) {
      if (window.CP_targetInput != null) {
        var a = new Date(e, t - 1, r, 0, 0, 0);
        (window.CP_calendarObject != null &&
          window.CP_calendarObject.copyMonthNamesToWindow(),
          (window.CP_targetInput.value = formatDate(a, window.CP_dateFormat)));
      } else
        alert(
          `Use setReturnFunction() to define which function will get the clicked results!`
        );
    }
    function CP_tmpReturnMonthFunction(e, t) {
      alert(
        `Use setReturnMonthFunction() to define which function will get the clicked results!
You clicked: year=` +
          e +
          ` , month=` +
          t
      );
    }
    function CP_tmpReturnQuarterFunction(e, t) {
      alert(
        `Use setReturnQuarterFunction() to define which function will get the clicked results!
You clicked: year=` +
          e +
          ` , quarter=` +
          t
      );
    }
    function CP_tmpReturnYearFunction(e) {
      alert(
        `Use setReturnYearFunction() to define which function will get the clicked results!
You clicked: year=` + e
      );
    }
    function CP_setReturnFunction(e) {
      this.returnFunction = e;
    }
    function CP_setReturnMonthFunction(e) {
      this.returnMonthFunction = e;
    }
    function CP_setReturnQuarterFunction(e) {
      this.returnQuarterFunction = e;
    }
    function CP_setReturnYearFunction(e) {
      this.returnYearFunction = e;
    }
    function CP_setMonthNames() {
      for (var e = 0; e < arguments.length; e++)
        this.monthNames[e] = arguments[e];
      this.copyMonthNamesToWindow();
    }
    function CP_setMonthAbbreviations() {
      for (var e = 0; e < arguments.length; e++)
        this.monthAbbreviations[e] = arguments[e];
      this.copyMonthNamesToWindow();
    }
    function CP_setDayHeaders() {
      for (var e = 0; e < arguments.length; e++)
        this.dayHeaders[e] = arguments[e];
    }
    function CP_setWeekStartDay(e) {
      this.weekStartDay = e;
    }
    function CP_showYearNavigation() {
      this.isShowYearNavigation = arguments.length > 0 ? arguments[0] : !0;
    }
    function CP_setDisplayType(e) {
      if (
        e != `date` &&
        e != `week-end` &&
        e != `month` &&
        e != `quarter` &&
        e != `year`
      )
        return (
          alert(
            `Invalid display type! Must be one of: date,week-end,month,quarter,year`
          ),
          !1
        );
      this.displayType = e;
    }
    function CP_setYearSelectStartOffset(e) {
      this.yearSelectStartOffset = e;
    }
    function CP_setDisabledWeekDays() {
      this.disabledWeekDays = {};
      for (var e = 0; e < arguments.length; e++)
        this.disabledWeekDays[arguments[e]] = !0;
    }
    function CP_addDisabledDates(e, t) {
      (arguments.length == 1 && (t = e),
        !(e == null && t == null) &&
          (this.disabledDatesExpression != `` &&
            (this.disabledDatesExpression += `||`),
          e != null &&
            ((e = parseDate(e)),
            (e =
              `` + e.getFullYear() + LZ(e.getMonth() + 1) + LZ(e.getDate()))),
          t != null &&
            ((t = parseDate(t)),
            (t =
              `` + t.getFullYear() + LZ(t.getMonth() + 1) + LZ(t.getDate()))),
          e == null
            ? (this.disabledDatesExpression += `(ds<=` + t + `)`)
            : t == null
              ? (this.disabledDatesExpression += `(ds>=` + e + `)`)
              : (this.disabledDatesExpression +=
                  `(ds>=` + e + `&&ds<=` + t + `)`)));
    }
    function CP_setTodayText(e) {
      this.todayText = e;
    }
    function CP_setCssPrefix(e) {
      this.cssPrefix = e;
    }
    function CP_showNavigationDropdowns() {
      this.isShowNavigationDropdowns = arguments.length > 0 ? arguments[0] : !0;
    }
    function CP_showYearNavigationInput() {
      this.isShowYearNavigationInput = arguments.length > 0 ? arguments[0] : !0;
    }
    function CP_hideCalendar() {
      arguments.length > 0
        ? window.popupWindowObjects[arguments[0]].hidePopup()
        : this.hidePopup();
    }
    function CP_refreshCalendar(e) {
      var t = window.popupWindowObjects[e];
      (arguments.length > 1
        ? t.populate(
            t.getCalendar(
              arguments[1],
              arguments[2],
              arguments[3],
              arguments[4],
              arguments[5]
            )
          )
        : t.populate(t.getCalendar()),
        t.refresh());
    }
    function CP_showCalendar(e) {
      (arguments.length > 1 &&
        (arguments[1] == null || arguments[1] == ``
          ? (this.currentDate = new Date())
          : (this.currentDate = new Date(parseDate(arguments[1])))),
        this.populate(this.getCalendar()),
        this.showPopup(e));
    }
    function CP_select(e, t, r) {
      var a = arguments.length > 3 ? arguments[3] : null;
      if (!window.getDateFromFormat) {
        alert(
          `calendar.select: To use this method you must also include 'date.js' for date formatting`
        );
        return;
      }
      if (this.displayType != `date` && this.displayType != `week-end`) {
        alert(
          `calendar.select: This function can only be used with displayType 'date' or 'week-end'`
        );
        return;
      }
      if (e.type != `text` && e.type != `hidden` && e.type != `textarea`) {
        (alert(
          `calendar.select: Input object passed is not a valid form input object`
        ),
          (window.CP_targetInput = null));
        return;
      }
      if (!e.disabled) {
        ((window.CP_targetInput = e),
          (window.CP_calendarObject = this),
          (this.currentDate = null));
        var o = 0;
        (a == null
          ? e.value != `` && (o = getDateFromFormat(e.value, r))
          : (o = getDateFromFormat(a, r)),
          (a != null || e.value != ``) &&
            (o == 0
              ? (this.currentDate = null)
              : (this.currentDate = new Date(o))),
          (window.CP_dateFormat = r),
          this.showCalendar(t));
      }
    }
    function getCalendarStyles() {
      var e = ``,
        t = ``;
      return (
        this != null &&
          this.cssPrefix !== void 0 &&
          this.cssPrefix != null &&
          this.cssPrefix != `` &&
          (t = this.cssPrefix),
        (e += `<STYLE>
`),
        (e +=
          `.` +
          t +
          `cpYearNavigation,.` +
          t +
          `cpMonthNavigation { background-color:#C0C0C0; text-align:center; vertical-align:center; text-decoration:none; color:#000000; font-weight:bold; }
`),
        (e +=
          `.` +
          t +
          `cpDayColumnHeader, .` +
          t +
          `cpYearNavigation,.` +
          t +
          `cpMonthNavigation,.` +
          t +
          `cpCurrentMonthDate,.` +
          t +
          `cpCurrentMonthDateDisabled,.` +
          t +
          `cpOtherMonthDate,.` +
          t +
          `cpOtherMonthDateDisabled,.` +
          t +
          `cpCurrentDate,.` +
          t +
          `cpCurrentDateDisabled,.` +
          t +
          `cpTodayText,.` +
          t +
          `cpTodayTextDisabled,.` +
          t +
          `cpText { font-family:arial; font-size:8pt; }
`),
        (e +=
          `TD.` +
          t +
          `cpDayColumnHeader { text-align:right; border:solid thin #C0C0C0;border-width:0px 0px 1px 0px; }
`),
        (e +=
          `.` +
          t +
          `cpCurrentMonthDate, .` +
          t +
          `cpOtherMonthDate, .` +
          t +
          `cpCurrentDate  { text-align:right; text-decoration:none; }
`),
        (e +=
          `.` +
          t +
          `cpCurrentMonthDateDisabled, .` +
          t +
          `cpOtherMonthDateDisabled, .` +
          t +
          `cpCurrentDateDisabled { color:#D0D0D0; text-align:right; text-decoration:line-through; }
`),
        (e +=
          `.` +
          t +
          `cpCurrentMonthDate, .cpCurrentDate { color:#000000; }
`),
        (e +=
          `.` +
          t +
          `cpOtherMonthDate { color:#808080; }
`),
        (e +=
          `TD.` +
          t +
          `cpCurrentDate { color:white; background-color: #C0C0C0; border-width:1px; border:solid thin #800000; }
`),
        (e +=
          `TD.` +
          t +
          `cpCurrentDateDisabled { border-width:1px; border:solid thin #FFAAAA; }
`),
        (e +=
          `TD.` +
          t +
          `cpTodayText, TD.` +
          t +
          `cpTodayTextDisabled { border:solid thin #C0C0C0; border-width:1px 0px 0px 0px;}
`),
        (e +=
          `A.` +
          t +
          `cpTodayText, SPAN.` +
          t +
          `cpTodayTextDisabled { height:20px; }
`),
        (e +=
          `A.` +
          t +
          `cpTodayText { color:black; }
`),
        (e +=
          `.` +
          t +
          `cpTodayTextDisabled { color:#D0D0D0; }
`),
        (e +=
          `.` +
          t +
          `cpBorder { border:solid thin #808080; }
`),
        (e += `</STYLE>
`),
        e
      );
    }
    function CP_getCalendar() {
      var now = new Date();
      if (this.type == `WINDOW`) var windowref = `window.opener.`;
      else var windowref = ``;
      var result = ``;
      if (
        (this.type == `WINDOW`
          ? ((result +=
              `<HTML><HEAD><TITLE>Calendar</TITLE>` +
              this.getStyles() +
              `</HEAD><BODY MARGINWIDTH=0 MARGINHEIGHT=0 TOPMARGIN=0 RIGHTMARGIN=0 LEFTMARGIN=0>
`),
            (result += `<CENTER><TABLE WIDTH=100% BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>
`))
          : ((result +=
              `<TABLE CLASS="` +
              this.cssPrefix +
              `cpBorder" WIDTH=144 BORDER=1 BORDERWIDTH=1 CELLSPACING=0 CELLPADDING=1>
`),
            (result += `<TR><TD ALIGN=CENTER>
`),
            (result += `<CENTER>
`)),
        this.displayType == `date` || this.displayType == `week-end`)
      ) {
        if (((this.currentDate ??= now), arguments.length > 0))
          var month = arguments[0];
        else var month = this.currentDate.getMonth() + 1;
        if (
          arguments.length > 1 &&
          arguments[1] > 0 &&
          arguments[1] - 0 == arguments[1]
        )
          var year = arguments[1];
        else var year = this.currentDate.getFullYear();
        var daysinmonth = [0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
        ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) &&
          (daysinmonth[2] = 29);
        var current_month = new Date(year, month - 1, 1),
          display_year = year,
          display_month = month,
          display_date = 1,
          weekday = current_month.getDay(),
          offset = 0;
        ((offset =
          weekday >= this.weekStartDay
            ? weekday - this.weekStartDay
            : 7 - this.weekStartDay + weekday),
          offset > 0 &&
            (display_month--,
            display_month < 1 && ((display_month = 12), display_year--),
            (display_date = daysinmonth[display_month] - offset + 1)));
        var next_month = month + 1,
          next_month_year = year;
        next_month > 12 && ((next_month = 1), next_month_year++);
        var last_month = month - 1,
          last_month_year = year;
        last_month < 1 && ((last_month = 12), last_month_year--);
        var date_class;
        (this.type != `WINDOW` &&
          (result += `<TABLE WIDTH=144 BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>`),
          (result += `<TR>
`));
        var refresh = windowref + `CP_refreshCalendar`,
          refreshLink = `javascript:` + refresh;
        if (this.isShowNavigationDropdowns) {
          result +=
            `<TD CLASS="` +
            this.cssPrefix +
            `cpMonthNavigation" WIDTH="78" COLSPAN="3"><select CLASS="` +
            this.cssPrefix +
            `cpMonthNavigation" name="cpMonth" onChange="` +
            refresh +
            `(` +
            this.index +
            `,this.options[this.selectedIndex].value-0,` +
            (year - 0) +
            `);">`;
          for (var monthCounter = 1; monthCounter <= 12; monthCounter++) {
            var selected = monthCounter == month ? `SELECTED` : ``;
            result +=
              `<option value="` +
              monthCounter +
              `" ` +
              selected +
              `>` +
              this.monthNames[monthCounter - 1] +
              `</option>`;
          }
          ((result += `</select></TD>`),
            (result +=
              `<TD CLASS="` +
              this.cssPrefix +
              `cpMonthNavigation" WIDTH="10">&nbsp;</TD>`),
            (result +=
              `<TD CLASS="` +
              this.cssPrefix +
              `cpYearNavigation" WIDTH="56" COLSPAN="3"><select CLASS="` +
              this.cssPrefix +
              `cpYearNavigation" name="cpYear" onChange="` +
              refresh +
              `(` +
              this.index +
              `,` +
              month +
              `,this.options[this.selectedIndex].value-0);">`));
          for (
            var yearCounter = 1900;
            yearCounter <= year + this.yearSelectStartOffset;
            yearCounter++
          ) {
            var selected = yearCounter == year ? `SELECTED` : ``;
            result +=
              `<option value="` +
              yearCounter +
              `" ` +
              selected +
              `>` +
              yearCounter +
              `</option>`;
          }
          result += `</select></TD>`;
        } else
          this.isShowYearNavigation
            ? ((result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="10"><A CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" HREF="` +
                refreshLink +
                `(` +
                this.index +
                `,` +
                last_month +
                `,` +
                last_month_year +
                `);">&lt;</A></TD>`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="58"><SPAN CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation">` +
                this.monthNames[month - 1] +
                `</SPAN></TD>`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="10"><A CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" HREF="` +
                refreshLink +
                `(` +
                this.index +
                `,` +
                next_month +
                `,` +
                next_month_year +
                `);">&gt;</A></TD>`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="10">&nbsp;</TD>`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpYearNavigation" WIDTH="10"><A CLASS="` +
                this.cssPrefix +
                `cpYearNavigation" HREF="` +
                refreshLink +
                `(` +
                this.index +
                `,` +
                month +
                `,` +
                (year - 1) +
                `);">&lt;</A></TD>`),
              this.isShowYearNavigationInput
                ? (result +=
                    `<TD CLASS="` +
                    this.cssPrefix +
                    `cpYearNavigation" WIDTH="36"><INPUT NAME="cpYear" CLASS="` +
                    this.cssPrefix +
                    `cpYearNavigation" SIZE="4" MAXLENGTH="4" VALUE="` +
                    year +
                    `" onBlur="` +
                    refresh +
                    `(` +
                    this.index +
                    `,` +
                    month +
                    `,this.value-0);"></TD>`)
                : (result +=
                    `<TD CLASS="` +
                    this.cssPrefix +
                    `cpYearNavigation" WIDTH="36"><SPAN CLASS="` +
                    this.cssPrefix +
                    `cpYearNavigation">` +
                    year +
                    `</SPAN></TD>`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpYearNavigation" WIDTH="10"><A CLASS="` +
                this.cssPrefix +
                `cpYearNavigation" HREF="` +
                refreshLink +
                `(` +
                this.index +
                `,` +
                month +
                `,` +
                (year + 1) +
                `);">&gt;</A></TD>`))
            : ((result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="22"><A CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" HREF="` +
                refreshLink +
                `(` +
                this.index +
                `,` +
                last_month +
                `,` +
                last_month_year +
                `);">&lt;&lt;</A></TD>
`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="100"><SPAN CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation">` +
                this.monthNames[month - 1] +
                ` ` +
                year +
                `</SPAN></TD>
`),
              (result +=
                `<TD CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" WIDTH="22"><A CLASS="` +
                this.cssPrefix +
                `cpMonthNavigation" HREF="` +
                refreshLink +
                `(` +
                this.index +
                `,` +
                next_month +
                `,` +
                next_month_year +
                `);">&gt;&gt;</A></TD>
`));
        ((result += `</TR></TABLE>
`),
          (result += `<TABLE WIDTH=120 BORDER=0 CELLSPACING=0 CELLPADDING=1 ALIGN=CENTER>
`),
          (result += `<TR>
`));
        for (var j = 0; j < 7; j++)
          result +=
            `<TD CLASS="` +
            this.cssPrefix +
            `cpDayColumnHeader" WIDTH="14%"><SPAN CLASS="` +
            this.cssPrefix +
            `cpDayColumnHeader">` +
            this.dayHeaders[(this.weekStartDay + j) % 7] +
            `</TD>
`;
        result += `</TR>
`;
        for (var row = 1; row <= 6; row++) {
          result += `<TR>
`;
          for (var col = 1; col <= 7; col++) {
            var disabled = !1;
            if (this.disabledDatesExpression != ``) {
              var ds = `` + display_year + LZ(display_month) + LZ(display_date);
              eval(`disabled=(` + this.disabledDatesExpression + `)`);
            }
            var dateClass = ``;
            if (
              ((dateClass =
                display_month == this.currentDate.getMonth() + 1 &&
                display_date == this.currentDate.getDate() &&
                display_year == this.currentDate.getFullYear()
                  ? `cpCurrentDate`
                  : display_month == month
                    ? `cpCurrentMonthDate`
                    : `cpOtherMonthDate`),
              disabled || this.disabledWeekDays[col - 1])
            )
              result +=
                `	<TD CLASS="` +
                this.cssPrefix +
                dateClass +
                `"><SPAN CLASS="` +
                this.cssPrefix +
                dateClass +
                `Disabled">` +
                display_date +
                `</SPAN></TD>
`;
            else {
              var selected_date = display_date,
                selected_month = display_month,
                selected_year = display_year;
              if (this.displayType == `week-end`) {
                var d = new Date(
                  selected_year,
                  selected_month - 1,
                  selected_date,
                  0,
                  0,
                  0,
                  0
                );
                (d.setDate(d.getDate() + (7 - col)),
                  (selected_year = d.getYear()),
                  selected_year < 1e3 && (selected_year += 1900),
                  (selected_month = d.getMonth() + 1),
                  (selected_date = d.getDate()));
              }
              result +=
                `	<TD CLASS="` +
                this.cssPrefix +
                dateClass +
                `"><A HREF="javascript:` +
                windowref +
                this.returnFunction +
                `(` +
                selected_year +
                `,` +
                selected_month +
                `,` +
                selected_date +
                `);` +
                windowref +
                `CP_hideCalendar('` +
                this.index +
                `');" CLASS="` +
                this.cssPrefix +
                dateClass +
                `">` +
                display_date +
                `</A></TD>
`;
            }
            (display_date++,
              display_date > daysinmonth[display_month] &&
                ((display_date = 1), display_month++),
              display_month > 12 && ((display_month = 1), display_year++));
          }
          result += `</TR>`;
        }
        var current_weekday = now.getDay() - this.weekStartDay;
        if (
          (current_weekday < 0 && (current_weekday += 7),
          (result += `<TR>
`),
          (result +=
            `	<TD COLSPAN=7 ALIGN=CENTER CLASS="` +
            this.cssPrefix +
            `cpTodayText">
`),
          this.disabledDatesExpression != ``)
        ) {
          var ds =
            `` + now.getFullYear() + LZ(now.getMonth() + 1) + LZ(now.getDate());
          eval(`disabled=(` + this.disabledDatesExpression + `)`);
        }
        (disabled || this.disabledWeekDays[current_weekday + 1]
          ? (result +=
              `		<SPAN CLASS="` +
              this.cssPrefix +
              `cpTodayTextDisabled">` +
              this.todayText +
              `</SPAN>
`)
          : (result +=
              `		<A CLASS="` +
              this.cssPrefix +
              `cpTodayText" HREF="javascript:` +
              windowref +
              this.returnFunction +
              `('` +
              now.getFullYear() +
              `','` +
              (now.getMonth() + 1) +
              `','` +
              now.getDate() +
              `');` +
              windowref +
              `CP_hideCalendar('` +
              this.index +
              `');">` +
              this.todayText +
              `</A>
`),
          (result += `		<BR>
`),
          (result += `	</TD></TR></TABLE></CENTER></TD></TR></TABLE>
`));
      }
      if (
        this.displayType == `month` ||
        this.displayType == `quarter` ||
        this.displayType == `year`
      ) {
        if (arguments.length > 0) var year = arguments[0];
        else if (this.displayType == `year`)
          var year = now.getFullYear() - this.yearSelectStartOffset;
        else var year = now.getFullYear();
        this.displayType != `year` &&
          this.isShowYearNavigation &&
          ((result += `<TABLE WIDTH=144 BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>`),
          (result += `<TR>
`),
          (result +=
            `	<TD CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" WIDTH="22"><A CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" HREF="javascript:` +
            windowref +
            `CP_refreshCalendar(` +
            this.index +
            `,` +
            (year - 1) +
            `);">&lt;&lt;</A></TD>
`),
          (result +=
            `	<TD CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" WIDTH="100">` +
            year +
            `</TD>
`),
          (result +=
            `	<TD CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" WIDTH="22"><A CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" HREF="javascript:` +
            windowref +
            `CP_refreshCalendar(` +
            this.index +
            `,` +
            (year + 1) +
            `);">&gt;&gt;</A></TD>
`),
          (result += `</TR></TABLE>
`));
      }
      if (this.displayType == `month`) {
        result += `<TABLE WIDTH=120 BORDER=0 CELLSPACING=1 CELLPADDING=0 ALIGN=CENTER>
`;
        for (var i = 0; i < 4; i++) {
          result += `<TR>`;
          for (var j = 0; j < 3; j++) {
            var monthindex = i * 3 + j;
            result +=
              `<TD WIDTH=33% ALIGN=CENTER><A CLASS="` +
              this.cssPrefix +
              `cpText" HREF="javascript:` +
              windowref +
              this.returnMonthFunction +
              `(` +
              year +
              `,` +
              (monthindex + 1) +
              `);` +
              windowref +
              `CP_hideCalendar('` +
              this.index +
              `');" CLASS="` +
              date_class +
              `">` +
              this.monthAbbreviations[monthindex] +
              `</A></TD>`;
          }
          result += `</TR>`;
        }
        result += `</TABLE></CENTER></TD></TR></TABLE>
`;
      }
      if (this.displayType == `quarter`) {
        result += `<BR><TABLE WIDTH=120 BORDER=1 CELLSPACING=0 CELLPADDING=0 ALIGN=CENTER>
`;
        for (var i = 0; i < 2; i++) {
          result += `<TR>`;
          for (var j = 0; j < 2; j++) {
            var quarter = i * 2 + j + 1;
            result +=
              `<TD WIDTH=50% ALIGN=CENTER><BR><A CLASS="` +
              this.cssPrefix +
              `cpText" HREF="javascript:` +
              windowref +
              this.returnQuarterFunction +
              `(` +
              year +
              `,` +
              quarter +
              `);` +
              windowref +
              `CP_hideCalendar('` +
              this.index +
              `');" CLASS="` +
              date_class +
              `">Q` +
              quarter +
              `</A><BR><BR></TD>`;
          }
          result += `</TR>`;
        }
        result += `</TABLE></CENTER></TD></TR></TABLE>
`;
      }
      if (this.displayType == `year`) {
        var yearColumnSize = 4;
        ((result += `<TABLE WIDTH=144 BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>`),
          (result += `<TR>
`),
          (result +=
            `	<TD CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" WIDTH="50%"><A CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" HREF="javascript:` +
            windowref +
            `CP_refreshCalendar(` +
            this.index +
            `,` +
            (year - yearColumnSize * 2) +
            `);">&lt;&lt;</A></TD>
`),
          (result +=
            `	<TD CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" WIDTH="50%"><A CLASS="` +
            this.cssPrefix +
            `cpYearNavigation" HREF="javascript:` +
            windowref +
            `CP_refreshCalendar(` +
            this.index +
            `,` +
            (year + yearColumnSize * 2) +
            `);">&gt;&gt;</A></TD>
`),
          (result += `</TR></TABLE>
`),
          (result += `<TABLE WIDTH=120 BORDER=0 CELLSPACING=1 CELLPADDING=0 ALIGN=CENTER>
`));
        for (var i = 0; i < yearColumnSize; i++) {
          for (var j = 0; j < 2; j++) {
            var currentyear = year + j * yearColumnSize + i;
            result +=
              `<TD WIDTH=50% ALIGN=CENTER><A CLASS="` +
              this.cssPrefix +
              `cpText" HREF="javascript:` +
              windowref +
              this.returnYearFunction +
              `(` +
              currentyear +
              `);` +
              windowref +
              `CP_hideCalendar('` +
              this.index +
              `');" CLASS="` +
              date_class +
              `">` +
              currentyear +
              `</A></TD>`;
          }
          result += `</TR>`;
        }
        result += `</TABLE></CENTER></TD></TR></TABLE>
`;
      }
      return (
        this.type == `WINDOW` &&
          (result += `</BODY></HTML>
`),
        result
      );
    }
  }),
  scriptRel,
  assetsURL,
  seen,
  __vitePreload,
  init_preload_helper = __esmMin(() => {
    ((scriptRel = `modulepreload`),
      (assetsURL = function (e) {
        return `/` + e;
      }),
      (seen = {}),
      (__vitePreload = function (e, t, r) {
        let a = Promise.resolve();
        if (t && t.length > 0) {
          let e = document.getElementsByTagName(`link`),
            o = document.querySelector(`meta[property=csp-nonce]`),
            c = o?.nonce || o?.getAttribute(`nonce`);
          function l(e) {
            return Promise.all(
              e.map((e) =>
                Promise.resolve(e).then(
                  (e) => ({ status: `fulfilled`, value: e }),
                  (e) => ({ status: `rejected`, reason: e })
                )
              )
            );
          }
          function u(e) {
            return import.meta.resolve
              ? import.meta.resolve(e)
              : new URL(
                  e,
                  new URL(
                    `../../../src/node/plugins/importAnalysisBuild.ts`,
                    import.meta.url
                  )
                ).href;
          }
          a = l(
            t.map((t) => {
              if (((t = assetsURL(t, r)), (t = u(t)), t in seen)) return;
              seen[t] = !0;
              let a = t.endsWith(`.css`);
              for (let r = e.length - 1; r >= 0; r--) {
                let o = e[r];
                if (o.href === t && (!a || o.rel === `stylesheet`)) return;
              }
              let o = document.createElement(`link`);
              if (
                ((o.rel = a ? `stylesheet` : scriptRel),
                a || (o.as = `script`),
                (o.crossOrigin = ``),
                (o.href = t),
                c && o.setAttribute(`nonce`, c),
                document.head.appendChild(o),
                a)
              )
                return new Promise((e, r) => {
                  (o.addEventListener(`load`, e),
                    o.addEventListener(`error`, () =>
                      r(Error(`Unable to preload CSS for ${t}`))
                    ));
                });
            })
          );
        }
        function o(e) {
          let t = new Event(`vite:preloadError`, { cancelable: !0 });
          if (((t.payload = e), window.dispatchEvent(t), !t.defaultPrevented))
            throw e;
        }
        return a.then((t) => {
          for (let e of t || []) e.status === `rejected` && o(e.reason);
          return e().catch(o);
        });
      }));
  }),
  require_main = __commonJSMin(() => {
    var e = require_prototype();
    init_scriptaculous();
    var t = require_setup_globals();
    (init_head_min(),
      init_jquery_tmpl(),
      init_jquery_migrate_module(),
      init_jquery_blockUI());
    var r = require_blockui_a11y();
    init_store();
    var a = require_json3(),
      o = require_jmesa(),
      c = require_new_cal(),
      l = require_wz_tooltip(),
      u = require_CalendarPopup();
    init_preload_helper();
    async function f() {
      let e =
          (window.app_studyOID !== void 0 &&
            document.title.includes(`Printable Forms`)) ||
          window.location.pathname.includes(`printcrf`),
        t = document.getElementById(`printCRFContainer`),
        r = document.getElementById(`menuContainer`),
        a = !!(e && t),
        o = !e && !!r;
      if (!o && !a) return;
      let [
        { default: c },
        { createRoot: l },
        { AccessibilityProvider: u },
        { default: f },
      ] = await Promise.all([
        __vitePreload(
          () =>
            import(`./react-vendor-CT9J5WVz.js`).then((e) => __toESM(e.r(), 1)),
          __vite__mapDeps([0, 1])
        ),
        __vitePreload(
          () =>
            import(`./react-vendor-CT9J5WVz.js`).then((e) => __toESM(e.n(), 1)),
          __vite__mapDeps([0, 1])
        ),
        __vitePreload(
          () => import(`./AccessibilityProvider-B4x-60D-.js`),
          __vite__mapDeps([2, 1, 0])
        ),
        __vitePreload(
          () => import(`./ErrorBoundary-CliOivNv.js`),
          __vite__mapDeps([3, 1, 0])
        ),
      ]);
      if (a) {
        let e = c.lazy(() =>
          __vitePreload(
            () => import(`./CRFRenderer-BpsNx76v.js`),
            __vite__mapDeps([4, 1, 0, 2, 5])
          )
        );
        l(t).render(
          c.createElement(
            f,
            null,
            c.createElement(
              u,
              null,
              c.createElement(
                c.Suspense,
                { fallback: c.createElement(`div`, null, `Loading CRF...`) },
                c.createElement(e, null)
              )
            )
          )
        );
      } else if (o) {
        let e = c.lazy(() =>
          __vitePreload(
            () => import(`./Navigation-BH5YU9Y7.js`),
            __vite__mapDeps([6, 1, 0, 7])
          )
        );
        l(r).render(
          c.createElement(
            f,
            null,
            c.createElement(
              u,
              null,
              c.createElement(
                c.Suspense,
                {
                  fallback: c.createElement(
                    `div`,
                    null,
                    `Loading Navigation...`
                  ),
                },
                c.createElement(e, null)
              )
            )
          )
        );
      }
    }
    document.readyState === `loading`
      ? document.addEventListener(`DOMContentLoaded`, () => {
          (f(), p());
        })
      : (f(), p());
    function p() {
      document
        .querySelectorAll(
          `input[tabindex], select[tabindex], textarea[tabindex], button[tabindex]`
        )
        .forEach((e) => {
          parseInt(e.getAttribute(`tabindex`), 10) > 0 &&
            e.removeAttribute(`tabindex`);
        });
    }
  });
export default require_main();
export { store as n, init_store as t };
