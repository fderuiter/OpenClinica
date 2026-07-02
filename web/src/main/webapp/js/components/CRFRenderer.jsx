import React, { useEffect, useState } from 'react';
import { store } from '../store';

export default function CRFRenderer() {
  const [studyOID, setStudyOID] = useState(store.getState().studyOID);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate loading CRF data
    const timer = setTimeout(() => {
      setLoading(false);
    }, 500);

    const unsubscribe = store.subscribe((state) => {
      setStudyOID(state.studyOID);
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
      {/* Ported rendering logic would go here */}
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
        <thead>
          <tr>
            <th style={{ borderBottom: '2px solid #ccc', textAlign: 'left', padding: '8px' }}>Item</th>
            <th style={{ borderBottom: '2px solid #ccc', textAlign: 'left', padding: '8px' }}>Value</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>Sample Question 1</td>
            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>[ ] Yes [ ] No</td>
          </tr>
          <tr>
            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>Sample Question 2</td>
            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>_________________</td>
          </tr>
        </tbody>
      </table>

      {/* Investigator Signature Block */}
      <div className="investigator-signature" style={{ marginTop: '40px', borderTop: '1px solid #000', paddingTop: '10px' }}>
        <p><strong>{window.app_investigatorLabel || 'Investigator'}:</strong> _________________________</p>
        <p><strong>{window.app_investigatorSignatureLabel || 'Investigator Signature'}:</strong> _________________________</p>
        <p><em>{window.app_meaning_of_signatureLabel || 'Meaning of Signature'}:</em> I attest that the data is accurate.</p>
      </div>
    </div>
  );
}
