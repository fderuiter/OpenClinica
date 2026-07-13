import React, { useEffect, useState } from 'react';
import { store } from '../store';

const schema = {
  formOID: "F_TEST_1",
  groups: [
    {
      groupOID: "IG_NON_REP",
      repeating: false,
      title: "General Info",
      fields: [
        {
          fieldOID: "I_GEN_NOTES",
          label: "Notes",
          type: "text"
        }
      ]
    },
    {
      groupOID: "IG_AE_1",
      repeating: true,
      title: "Adverse Events",
      fields: [
        {
          fieldOID: "I_AE_TERM",
          label: "Adverse Event Term",
          type: "text"
        },
        {
          fieldOID: "I_AE_SEVERITY",
          label: "Severity",
          type: "select",
          options: ["Mild", "Moderate", "Severe"]
        },
        {
          fieldOID: "I_AE_ONSET",
          label: "Onset Date",
          type: "date"
        }
      ]
    }
  ]
};

export default function CRFRenderer() {
  const [studyOID, setStudyOID] = useState(store.getState().studyOID);
  const [formData, setFormData] = useState(store.getState().formData);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Initialize default row for repeating groups if empty
    schema.groups.forEach(group => {
      const data = store.getState().formData[group.groupOID];
      if (!data || data.length === 0) {
        if (group.repeating) {
          // No initial rows required by default unless specified, but let's initialize 1 for user convenience
          // Wait, acceptance criteria says "click a button to add an entry". We'll just leave it empty initially or let addRow handle it.
        } else {
          // Initialize non-repeating group with 1 row
          store.addRow(group.groupOID, schema);
        }
      }
    });

    const timer = setTimeout(() => {
      setLoading(false);
    }, 500);

    const unsubscribe = store.subscribe((state) => {
      setStudyOID(state.studyOID);
      setFormData(state.formData);
    });

    return () => {
      clearTimeout(timer);
      unsubscribe();
    };
  }, []);

  if (loading) {
    return <div className="spinner">Loading CRF Data...</div>;
  }

  return (
    <div className="crf-renderer" style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h1>Printable CRF View</h1>
      <div className="crf-details">
        <h2>Study Details</h2>
        <p><strong>Study OID:</strong> {studyOID}</p>
        <p>This is a modernized reactive rendering of the Case Report Form (CRF).</p>
      </div>

      <form onSubmit={(e) => e.preventDefault()}>
        {schema.groups.map(group => {
          const rows = formData[group.groupOID] || [];
          return (
            <div key={group.groupOID} style={{ marginTop: '20px', border: '1px solid #ccc', padding: '10px' }}>
              <h3>{group.title}</h3>
              {rows.map((row, index) => (
                <div key={index} style={{ marginBottom: '15px', padding: '10px', backgroundColor: '#f9f9f9' }}>
                  {group.repeating && <h4>Row {index + 1}</h4>}
                  {group.fields.map(field => {
                    const fieldId = `${group.groupOID}[${index}].${field.fieldOID}`;
                    return (
                      <div key={field.fieldOID} style={{ marginBottom: '10px' }}>
                        <label htmlFor={fieldId} style={{ display: 'inline-block', width: '150px' }}>{field.label}:</label>
                        {field.type === 'select' ? (
                          <select
                            id={fieldId}
                            name={fieldId}
                            value={row[field.fieldOID] || ''}
                            onChange={(e) => store.setFormData(group.groupOID, index, field.fieldOID, e.target.value)}
                          >
                            <option value="">--Select--</option>
                            {field.options.map(opt => <option key={opt} value={opt}>{opt}</option>)}
                          </select>
                        ) : (
                          <input
                            id={fieldId}
                            name={fieldId}
                            type={field.type}
                            value={row[field.fieldOID] || ''}
                            onChange={(e) => store.setFormData(group.groupOID, index, field.fieldOID, e.target.value)}
                          />
                        )}
                      </div>
                    );
                  })}
                  {group.repeating && (
                    <button type="button" onClick={() => store.removeRow(group.groupOID, index)}>Remove Row</button>
                  )}
                </div>
              ))}
              {group.repeating && (
                <button type="button" onClick={() => store.addRow(group.groupOID, schema)}>Add {group.title} Entry</button>
              )}
            </div>
          );
        })}
      </form>

      {/* Investigator Signature Block */}
      <div className="investigator-signature" style={{ marginTop: '40px', borderTop: '1px solid #000', paddingTop: '10px' }}>
        <p><strong>{window.app_investigatorLabel || 'Investigator'}:</strong> _________________________</p>
        <p><strong>{window.app_investigatorSignatureLabel || 'Investigator Signature'}:</strong> _________________________</p>
        <p><em>{window.app_meaning_of_signatureLabel || 'Meaning of Signature'}:</em> I attest that the data is accurate.</p>
      </div>
    </div>
  );
}
